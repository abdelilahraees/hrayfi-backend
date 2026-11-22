package org.example.backend.paiement.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.admin.dto.ChangerStatutRequest;
import org.example.backend.admin.service.AdminService;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.annonce.repository.AnnonceRepository;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.common.exception.UnauthorizedActionException;
import org.example.backend.notification.entity.NotificationType;
import org.example.backend.notification.service.NotificationService;
import org.example.backend.paiement.dto.*;
import org.example.backend.paiement.entity.*;
import org.example.backend.paiement.repository.DemandeAbonnementRepository;
import org.example.backend.user.entity.Artisan;
import org.example.backend.user.entity.Role;
import org.example.backend.user.entity.StatutValidation;
import org.example.backend.user.repository.AppUserRepository;
import org.example.backend.user.repository.ArtisanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Demandes de changement d'offre : l'artisan choisit une formule depuis son espace, l'équipe est notifiée
 * puis décide (contacter, encaisser, refuser, suspendre le compte). Chaque décision est notifiée à l'artisan.
 */
@Service
@RequiredArgsConstructor
public class DemandeAbonnementService {

    private static final List<StatutDemandeAbonnement> OUVERTES =
            List.of(StatutDemandeAbonnement.EN_ATTENTE, StatutDemandeAbonnement.CONTACTEE);

    private final DemandeAbonnementRepository repository;
    private final ArtisanRepository artisanRepository;
    private final AnnonceRepository annonceRepository;
    private final AppUserRepository userRepository;
    private final AdminService adminService;
    private final NotificationService notificationService;

    public List<FormuleDTO> formules() {
        return Arrays.stream(Formule.values()).map(FormuleDTO::of).toList();
    }

    // ---------- artisan ----------

    @Transactional
    public DemandeAbonnementDTO creer(Long artisanId, CreerDemandeAbonnementRequest req) {
        Artisan artisan = artisanRepository.findById(artisanId).orElseThrow(() -> ResourceNotFoundException.of("Artisan", artisanId));
        if (repository.existsByArtisanIdAndStatutIn(artisanId, OUVERTES)) {
            throw new BusinessException("Vous avez déjà une demande en cours : l'équipe vous recontacte. Vous pouvez l'annuler pour en faire une autre.");
        }
        Annonce annonce = null;
        if (req.formule().objet() == ObjetPaiement.MISE_EN_AVANT) {
            if (req.annonceId() == null) throw new BusinessException("Choisissez l'annonce à mettre en avant.");
            annonce = annonceRepository.findById(req.annonceId())
                    .filter(a -> a.getArtisan().getId().equals(artisanId))
                    .orElseThrow(() -> new BusinessException("Cette annonce ne vous appartient pas."));
        }
        DemandeAbonnement d = repository.save(DemandeAbonnement.builder()
                .artisan(artisan).formule(req.formule()).annonce(annonce).moyenSouhaite(req.moyenSouhaite())
                .message(StringUtils.hasText(req.message()) ? req.message().trim() : null)
                .build());

        String nom = artisan.getPrenom() + " " + artisan.getNom();
        userRepository.findByRole(Role.ADMIN).forEach(admin -> notificationService.create(admin, NotificationType.DEMANDE_OFFRE,
                "Nouvelle demande d'offre",
                nom + " demande « " + req.formule().libelle() + " » (" + req.formule().montant() + " MAD).",
                "/espace/admin?onglet=offres"));
        return toDto(d);
    }

    @Transactional(readOnly = true)
    public List<DemandeAbonnementDTO> mesDemandes(Long artisanId) {
        return repository.findByArtisanIdOrderByCreatedAtDesc(artisanId).stream().map(this::toDto).toList();
    }

    @Transactional
    public DemandeAbonnementDTO annuler(Long artisanId, Long id) {
        DemandeAbonnement d = load(id);
        if (!d.getArtisan().getId().equals(artisanId)) throw new UnauthorizedActionException("Cette demande ne vous appartient pas");
        if (!d.isOuverte()) throw new BusinessException("Cette demande est déjà traitée.");
        d.setStatut(StatutDemandeAbonnement.ANNULEE);
        return toDto(d);
    }

    // ---------- administration ----------

    @Transactional(readOnly = true)
    public Page<DemandeAbonnementDTO> liste(StatutDemandeAbonnement statut, Pageable pageable) {
        return repository.adminSearch(statut, pageable).map(this::toDto);
    }

    @Transactional
    public DemandeAbonnementDTO traiter(Long adminId, Long id, TraiterDemandeRequest req) {
        DemandeAbonnement d = load(id);
        if (!d.isOuverte()) throw new BusinessException("Cette demande est déjà traitée.");
        Artisan artisan = d.getArtisan();
        String reponse = StringUtils.hasText(req.reponse()) ? req.reponse().trim() : null;
        Formule f = d.getFormule();

        switch (req.decision()) {
            case CONTACTEE -> {
                d.setStatut(StatutDemandeAbonnement.CONTACTEE);
                notifier(artisan, "Votre demande d'offre est en cours",
                        reponse != null ? reponse : "L'équipe Hrayfi vous contacte pour le paiement de « " + f.libelle() + " ».");
            }
            case PAYEE -> {
                // Enregistre le paiement : prolonge le Pro ou met l'annonce en avant, et notifie l'artisan
                Paiement p = adminService.enregistrer(adminId, artisan.getId(), new EnregistrerPaiementRequest(
                        f.objet(), f.montant(), req.moyen() != null ? req.moyen() : d.getMoyenSouhaite(), req.reference(),
                        f.dureeJours(), d.getAnnonce() == null ? null : d.getAnnonce().getId(), "Demande d'offre n° " + d.getId()));
                d.setPaiement(p);
                d.setStatut(StatutDemandeAbonnement.PAYEE);
            }
            case REFUSEE -> {
                if (reponse == null) throw new BusinessException("Expliquez le refus à l'artisan.");
                d.setStatut(StatutDemandeAbonnement.REFUSEE);
                notifier(artisan, "Votre demande d'offre n'a pas abouti", reponse);
            }
            case SUSPENDRE_COMPTE -> {
                if (reponse == null) throw new BusinessException("Indiquez le motif de la suspension : il sera montré à l'artisan.");
                adminService.changerStatut(artisan.getId(), new ChangerStatutRequest(StatutValidation.SUSPENDU, reponse));
                d.setStatut(StatutDemandeAbonnement.REFUSEE);
            }
        }
        d.setReponseAdmin(reponse);
        d.setTraiteePar(userRepository.getReferenceById(adminId));
        d.setTraiteeLe(Instant.now());
        return toDto(d);
    }

    // ---------- helpers ----------

    private void notifier(Artisan artisan, String titre, String message) {
        notificationService.create(artisan, NotificationType.DEMANDE_OFFRE_TRAITEE, titre, message, "/espace/artisan");
    }

    private DemandeAbonnement load(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Demande d'offre", id));
    }

    private DemandeAbonnementDTO toDto(DemandeAbonnement d) {
        Artisan a = d.getArtisan();
        return new DemandeAbonnementDTO(
                d.getId(), a.getId(), (a.getPrenom() + " " + a.getNom()).trim(), a.getTelephone(), a.getMetier(), a.getVille(),
                a.getStatutValidation(), a.getStatutPro(), a.getProJusquAu(),
                FormuleDTO.of(d.getFormule()),
                d.getAnnonce() == null ? null : d.getAnnonce().getId(),
                d.getAnnonce() == null ? null : d.getAnnonce().getTitre(),
                d.getMoyenSouhaite(), d.getMessage(), d.getStatut(), d.getReponseAdmin(),
                d.getTraiteePar() == null ? null : (d.getTraiteePar().getPrenom() + " " + d.getTraiteePar().getNom()).trim(),
                d.getTraiteeLe(), d.getCreatedAt());
    }
}
