package org.example.backend.admin.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.admin.dto.*;
import org.example.backend.annonce.dto.AnnonceDTO;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.annonce.mapper.AnnonceMapper;
import org.example.backend.annonce.repository.AnnonceRepository;
import org.example.backend.annonce.service.AnnonceService;
import org.example.backend.avis.dto.AvisDTO;
import org.example.backend.avis.repository.AvisAnnonceRepository;
import org.example.backend.avis.service.AvisService;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.demande.dto.DemandeServiceDTO;
import org.example.backend.demande.entity.StatutDemande;
import org.example.backend.demande.mapper.DemandeServiceMapper;
import org.example.backend.demande.repository.DemandeServiceRepository;
import org.example.backend.notification.entity.NotificationType;
import org.example.backend.notification.service.NotificationService;
import org.example.backend.paiement.dto.EnregistrerPaiementRequest;
import org.example.backend.paiement.dto.PaiementMapper;
import org.example.backend.paiement.entity.ObjetPaiement;
import org.example.backend.paiement.entity.Paiement;
import org.example.backend.paiement.repository.PaiementRepository;
import org.example.backend.user.entity.*;
import org.example.backend.user.repository.AppUserRepository;
import org.example.backend.user.repository.ArtisanRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Administration de la plateforme : validation des artisans, consultation des comptes,
 * modération des annonces et des avis, suivi des demandes.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AppUserRepository userRepository;
    private final ArtisanRepository artisanRepository;
    private final AnnonceRepository annonceRepository;
    private final AvisAnnonceRepository avisRepository;
    private final DemandeServiceRepository demandeRepository;
    private final AnnonceService annonceService;
    private final AvisService avisService;
    private final NotificationService notificationService;
    private final AnnonceMapper annonceMapper;
    private final DemandeServiceMapper demandeMapper;
    private final PaiementRepository paiementRepository;

    /** Durée de l'offre Pro offerte à la validation d'un artisan */
    @Value("${app.pro.offert-jours:90}")
    private int proOffertJours;

    /** Un compte est « à relancer » quand son échéance est passée ou tombe dans ce délai */
    private static final int RELANCE_JOURS = 15;

    @Transactional(readOnly = true)
    public AdminStatsDTO stats() {
        Map<String, Long> artisansParStatut = new LinkedHashMap<>();
        Arrays.stream(StatutValidation.values())
                .forEach(s -> artisansParStatut.put(s.name(), artisanRepository.countByStatutValidation(s)));
        Map<String, Long> demandesParStatut = new LinkedHashMap<>();
        Arrays.stream(StatutDemande.values())
                .forEach(s -> demandesParStatut.put(s.name(), demandeRepository.countByStatut(s)));
        Double moyenne = avisRepository.noteMoyenneGlobale();
        return new AdminStatsDTO(
                userRepository.countByRole(Role.CLIENT),
                userRepository.countByRole(Role.ARTISAN),
                artisansParStatut,
                annonceRepository.count(),
                annonceRepository.countByActiveTrue(),
                demandeRepository.count(),
                demandesParStatut,
                avisRepository.count(),
                moyenne == null ? 0 : Math.round(moyenne * 100.0) / 100.0,
                demandeRepository.volumeAffaires(),
                paiementRepository.totalDepuis(debutDuMois(0)),
                paiementRepository.totalDepuis(Instant.EPOCH),
                artisanRepository.countByStatutValidationAndProOffertAndProJusquAuAfter(StatutValidation.VALIDE, false, Instant.now()),
                artisanRepository.countByStatutValidationAndProOffertAndProJusquAuAfter(StatutValidation.VALIDE, true, Instant.now()),
                artisanRepository.countARelancer(Instant.now().plus(RELANCE_JOURS, ChronoUnit.DAYS)),
                annonceRepository.countBySponsoriseJusquAuAfter(Instant.now()),
                revenusParMois()
        );
    }

    /** 6 derniers mois, y compris les mois sans paiement */
    private List<AdminStatsDTO.RevenuMois> revenusParMois() {
        Map<String, BigDecimal> totaux = new HashMap<>();
        for (Object[] r : paiementRepository.revenusParMois(debutDuMois(5))) {
            totaux.put(((Number) r[0]).intValue() + "-" + ((Number) r[1]).intValue(), (BigDecimal) r[2]);
        }
        List<AdminStatsDTO.RevenuMois> mois = new ArrayList<>();
        YearMonth courant = YearMonth.now(ZoneId.systemDefault());
        for (int i = 5; i >= 0; i--) {
            YearMonth m = courant.minusMonths(i);
            mois.add(new AdminStatsDTO.RevenuMois(m.getYear(), m.getMonthValue(),
                    totaux.getOrDefault(m.getYear() + "-" + m.getMonthValue(), BigDecimal.ZERO)));
        }
        return mois;
    }

    private static Instant debutDuMois(int moisAvant) {
        return YearMonth.now(ZoneId.systemDefault()).minusMonths(moisAvant).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    // ---------- artisans ----------

    @Transactional(readOnly = true)
    public Page<AdminArtisanDTO> artisans(StatutValidation statut, String q, Pageable pageable) {
        return artisanRepository.adminSearch(statut, like(q), pageable).map(this::toAdminArtisan);
    }

    /**
     * Change le statut d'un artisan. Un refus ou une suspension exige un motif, montré à l'artisan
     * lorsqu'il tente de se connecter ; une validation lui envoie une notification.
     */
    @Transactional
    public AdminArtisanDTO changerStatut(Long artisanId, ChangerStatutRequest req) {
        Artisan artisan = artisanRepository.findById(artisanId)
                .orElseThrow(() -> ResourceNotFoundException.of("Artisan", artisanId));
        StatutValidation statut = req.statut();
        boolean motifRequis = statut == StatutValidation.REFUSE || statut == StatutValidation.SUSPENDU;
        if (motifRequis && !StringUtils.hasText(req.motif())) {
            throw new BusinessException("Indiquez un motif : il sera communiqué à l'artisan.");
        }

        artisan.setStatutValidation(statut);
        artisan.setMotifRefus(motifRequis ? req.motif().trim() : null);
        if (statut == StatutValidation.VALIDE) {
            artisan.setValideLe(Instant.now());
            if (artisan.getProJusquAu() == null) {
                // Offre Pro offerte pendant la période de lancement ; l'équipe relance ensuite pour le paiement
                artisan.setProJusquAu(Instant.now().plus(proOffertJours, ChronoUnit.DAYS));
                artisan.setProOffert(true);
            }
            notificationService.create(artisan, NotificationType.COMPTE_VALIDE,
                    "Votre compte est validé",
                    "Bienvenue sur Hrayfi ! Publiez votre première annonce pour recevoir des demandes de clients.",
                    "/espace/artisan/annonces/nouvelle");
        }
        return toAdminArtisan(artisan);
    }

    // ---------- comptes et paiements ----------

    @Transactional(readOnly = true)
    public Page<AdminCompteDTO> comptes(String filtre, String q, Pageable pageable) {
        String f = List.of("A_RELANCER", "PAYES", "OFFERTS", "EXPIRES").contains(filtre) ? filtre : "TOUS";
        Instant maintenant = Instant.now();
        return artisanRepository.comptes(f, like(q), maintenant, maintenant.plus(RELANCE_JOURS, ChronoUnit.DAYS), pageable)
                .map(this::toCompte);
    }

    @Transactional(readOnly = true)
    public AdminCompteDetailDTO compte(Long artisanId) {
        Artisan a = loadArtisan(artisanId);
        return new AdminCompteDetailDTO(
                toCompte(a),
                paiementRepository.findByArtisanIdOrderByCreatedAtDesc(artisanId).stream().map(PaiementMapper::toDto).toList(),
                annonceRepository.findByArtisanIdOrderByCreatedAtDesc(artisanId).stream()
                        .map(an -> new AdminCompteDetailDTO.AnnonceResume(an.getId(), an.getTitre(), an.isActive(), an.getSponsoriseJusquAu()))
                        .toList());
    }

    /**
     * Enregistre un paiement encaissé hors ligne. Abonnement : prolonge l'offre Pro à partir de l'échéance
     * (ou d'aujourd'hui si elle est passée). Mise en avant : idem pour l'annonce choisie. L'artisan est notifié.
     */
    @Transactional
    public AdminCompteDetailDTO enregistrerPaiement(Long adminId, Long artisanId, EnregistrerPaiementRequest req) {
        Artisan artisan = loadArtisan(artisanId);
        Annonce annonce = null;
        String message;
        if (req.objet() == ObjetPaiement.MISE_EN_AVANT) {
            if (req.annonceId() == null) throw new BusinessException("Choisissez l'annonce à mettre en avant.");
            annonce = annonceRepository.findById(req.annonceId())
                    .filter(an -> an.getArtisan().getId().equals(artisanId))
                    .orElseThrow(() -> new BusinessException("Cette annonce n'appartient pas à cet artisan."));
            annonce.setSponsoriseJusquAu(prolonger(annonce.getSponsoriseJusquAu(), req.dureeJours()));
            message = "Votre annonce « " + annonce.getTitre() + " » est mise en avant jusqu'au " + date(annonce.getSponsoriseJusquAu()) + ".";
        } else {
            artisan.setProJusquAu(prolonger(artisan.getProJusquAu(), req.dureeJours()));
            artisan.setProOffert(false);
            message = "Votre offre Pro est prolongée jusqu'au " + date(artisan.getProJusquAu()) + ". Merci !";
        }
        paiementRepository.save(Paiement.builder()
                .artisan(artisan).objet(req.objet()).montant(req.montant()).moyen(req.moyen())
                .reference(StringUtils.hasText(req.reference()) ? req.reference().trim() : null)
                .dureeJours(req.dureeJours()).annonce(annonce)
                .note(StringUtils.hasText(req.note()) ? req.note().trim() : null)
                .enregistrePar(userRepository.getReferenceById(adminId))
                .build());
        notificationService.create(artisan, NotificationType.PAIEMENT_RECU, "Paiement reçu", message, "/espace/artisan");
        return compte(artisanId);
    }

    @Transactional
    public AdminCompteDTO modifierNote(Long artisanId, String note) {
        Artisan a = loadArtisan(artisanId);
        a.setNoteAdmin(StringUtils.hasText(note) ? note.trim() : null);
        return toCompte(a);
    }

    private static Instant prolonger(Instant echeance, int jours) {
        Instant depart = echeance == null || echeance.isBefore(Instant.now()) ? Instant.now() : echeance;
        return depart.plus(jours, ChronoUnit.DAYS);
    }

    private static String date(Instant i) {
        return DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH).withZone(ZoneId.systemDefault()).format(i);
    }

    private Artisan loadArtisan(Long id) {
        return artisanRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Artisan", id));
    }

    private AdminCompteDTO toCompte(Artisan a) {
        return new AdminCompteDTO(
                a.getId(), a.getPrenom(), a.getNom(), a.getEmail(), a.getTelephone(), a.getMetier(), a.getVille(),
                a.getStatutPro(), a.getProJusquAu(), a.isProOffert(),
                paiementRepository.totalArtisan(a.getId()),
                annonceRepository.countByArtisanId(a.getId()),
                annonceRepository.findByArtisanIdOrderByCreatedAtDesc(a.getId()).stream().filter(Annonce::isSponsorisee).count(),
                a.getNoteAdmin(), a.getCreatedAt());
    }

    // ---------- utilisateurs ----------

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> utilisateurs(Role role, String q, Pageable pageable) {
        return userRepository.adminSearch(role, like(q), pageable).map(u -> new AdminUserDTO(
                u.getId(), u.getPrenom(), u.getNom(), u.getEmail(), u.getRole(), u.getCreatedAt(),
                u instanceof Artisan a ? a.getStatutValidation() : null));
    }

    // ---------- annonces ----------

    @Transactional(readOnly = true)
    public Page<AnnonceDTO> annonces(String q, Pageable pageable) {
        return annonceRepository.adminSearch(like(q), pageable).map(annonceMapper::toDto);
    }

    public AnnonceDTO changerVisibilite(Long annonceId, boolean active) {
        return annonceService.changerVisibiliteAdmin(annonceId, active);
    }

    public void supprimerAnnonce(Long annonceId) {
        annonceService.supprimerAdmin(annonceId);
    }

    // ---------- avis ----------

    @Transactional(readOnly = true)
    public Page<AvisDTO> avis(Integer noteMax, String q, Pageable pageable) {
        int max = noteMax == null ? 5 : Math.max(1, Math.min(5, noteMax));
        return avisRepository.adminSearch(max, like(q), pageable).map(avisService::versDto);
    }

    public void supprimerAvis(Long avisId) {
        avisService.supprimerAdmin(avisId);
    }

    // ---------- demandes ----------

    @Transactional(readOnly = true)
    public Page<DemandeServiceDTO> demandes(StatutDemande statut, Pageable pageable) {
        return demandeRepository.adminSearch(statut, pageable).map(demandeMapper::toDto);
    }

    // ---------- helpers ----------

    private AdminArtisanDTO toAdminArtisan(Artisan a) {
        return new AdminArtisanDTO(
                a.getId(), a.getPrenom(), a.getNom(), a.getEmail(), a.getMetier(), a.getVille(),
                a.getTarifHoraire(), a.getDescription(), a.getCreatedAt(),
                a.getStatutValidation(), a.getMotifRefus(), a.getValideLe(),
                annonceRepository.countByArtisanId(a.getId()));
    }

    /** Motif LIKE en minuscules, ou "" pour ignorer le filtre. */
    private static String like(String q) {
        return StringUtils.hasText(q) ? "%" + q.trim().toLowerCase() + "%" : "";
    }
}
