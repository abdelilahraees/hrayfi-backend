package org.example.backend.demande.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.annonce.repository.AnnonceRepository;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.common.exception.UnauthorizedActionException;
import org.example.backend.demande.dto.CreateDemandeRequest;
import org.example.backend.demande.dto.DemandeServiceDTO;
import org.example.backend.demande.entity.DemandeService;
import org.example.backend.demande.entity.StatutDemande;
import org.example.backend.demande.mapper.DemandeServiceMapper;
import org.example.backend.demande.repository.DemandeServiceRepository;
import org.example.backend.notification.entity.NotificationType;
import org.example.backend.notification.service.NotificationService;
import org.example.backend.user.entity.Artisan;
import org.example.backend.user.entity.Client;
import org.example.backend.user.repository.ArtisanRepository;
import org.example.backend.user.repository.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Orchestration du cycle de vie d'une {@link DemandeService}.
 * Les transitions de statut sont strictement contrôlées :
 *   EN_ATTENTE ─accepter→ ACCEPTEE ─terminer→ TERMINEE
 *   EN_ATTENTE ─refuser→  REFUSEE
 */
@Service
@RequiredArgsConstructor
public class DemandeServiceService {

    private final DemandeServiceRepository demandeRepository;
    private final ClientRepository clientRepository;
    private final ArtisanRepository artisanRepository;
    private final DemandeServiceMapper mapper;
    private final NotificationService notificationService;
    private final AnnonceRepository annonceRepository;

    @Transactional
    public DemandeServiceDTO creerDemande(Long clientId, CreateDemandeRequest req) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> ResourceNotFoundException.of("Client", clientId));
        Artisan artisan = artisanRepository.findById(req.artisanId())
                .orElseThrow(() -> ResourceNotFoundException.of("Artisan", req.artisanId()));

        Annonce annonce = null;
        if (req.annonceId() != null) {
            annonce = annonceRepository.findById(req.annonceId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Annonce", req.annonceId()));
            if (!annonce.getArtisan().getId().equals(artisan.getId())) {
                throw new IllegalArgumentException("Cette annonce n'appartient pas à l'artisan choisi");
            }
        }

        DemandeService demande = DemandeService.builder()
                .client(client)
                .artisan(artisan)
                .annonce(annonce)
                .description(req.description())
                .dateIntervention(req.dateIntervention())
                .prixConvenu(req.prixConvenu())
                .statut(StatutDemande.EN_ATTENTE)
                .build();

        DemandeService saved = demandeRepository.save(demande);

        notificationService.create(
                artisan,
                NotificationType.NOUVELLE_DEMANDE,
                "Nouvelle demande de service",
                "%s %s souhaite votre intervention"
                        .formatted(client.getPrenom(), client.getNom()),
                "/demandes/" + saved.getId()
        );
        return mapper.toDto(saved);
    }

    @Transactional
    public DemandeServiceDTO accepterDemande(Long demandeId, Long artisanId) {
        DemandeService demande = loadForArtisan(demandeId, artisanId);
        requireStatut(demande, StatutDemande.EN_ATTENTE, "Seule une demande en attente peut être acceptée");
        demande.setStatut(StatutDemande.ACCEPTEE);
        notifierClient(demande, NotificationType.DEMANDE_ACCEPTEE,
                "Demande acceptée",
                "L'artisan a accepté votre demande.");
        return mapper.toDto(demande);
    }

    @Transactional
    public DemandeServiceDTO refuserDemande(Long demandeId, Long artisanId) {
        DemandeService demande = loadForArtisan(demandeId, artisanId);
        requireStatut(demande, StatutDemande.EN_ATTENTE, "Seule une demande en attente peut être refusée");
        demande.setStatut(StatutDemande.REFUSEE);
        notifierClient(demande, NotificationType.DEMANDE_REFUSEE,
                "Demande refusée",
                "L'artisan n'est pas disponible pour cette demande.");
        return mapper.toDto(demande);
    }

    @Transactional
    public DemandeServiceDTO terminerDemande(Long demandeId, Long artisanId, BigDecimal prixFinal) {
        DemandeService demande = loadForArtisan(demandeId, artisanId);
        requireStatut(demande, StatutDemande.ACCEPTEE, "Seule une demande acceptée peut être terminée");
        demande.setStatut(StatutDemande.TERMINEE);
        demande.setPrixFinal(prixFinal);
        notifierClient(demande, NotificationType.DEMANDE_TERMINEE,
                "Intervention terminée",
                "Montant final : " + prixFinal + " MAD. Vous pouvez maintenant évaluer l'artisan.");
        return mapper.toDto(demande);
    }

    private void notifierClient(DemandeService demande, NotificationType type,
                                 String titre, String message) {
        notificationService.create(demande.getClient(), type, titre, message,
                "/demandes/" + demande.getId());
    }

    @Transactional(readOnly = true)
    public Page<DemandeServiceDTO> listerParClient(Long clientId, Pageable pageable) {
        return demandeRepository.findByClientId(clientId, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<DemandeServiceDTO> listerParArtisan(Long artisanId, Pageable pageable) {
        return demandeRepository.findByArtisanId(artisanId, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public DemandeServiceDTO getById(Long demandeId, Long currentUserId) {
        DemandeService demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> ResourceNotFoundException.of("Demande", demandeId));
        boolean estProprietaire =
                demande.getClient().getId().equals(currentUserId) ||
                demande.getArtisan().getId().equals(currentUserId);
        if (!estProprietaire) {
            throw new UnauthorizedActionException("Vous n'avez pas accès à cette demande");
        }
        return mapper.toDto(demande);
    }

    // --- helpers ---

    private DemandeService loadForArtisan(Long demandeId, Long artisanId) {
        DemandeService demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> ResourceNotFoundException.of("Demande", demandeId));
        if (!demande.getArtisan().getId().equals(artisanId)) {
            throw new UnauthorizedActionException("Cette demande ne vous est pas destinée");
        }
        return demande;
    }

    private void requireStatut(DemandeService demande, StatutDemande attendu, String message) {
        if (demande.getStatut() != attendu) {
            throw new BusinessException(message + " (statut actuel : " + demande.getStatut() + ")");
        }
    }
}