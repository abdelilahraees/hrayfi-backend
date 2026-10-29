package org.example.backend.avis.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.annonce.repository.AnnonceRepository;
import org.example.backend.avis.dto.AvisDTO;
import org.example.backend.avis.dto.AvisRequest;
import org.example.backend.avis.entity.AvisAnnonce;
import org.example.backend.avis.mapper.AvisMapper;
import org.example.backend.avis.repository.AvisAnnonceRepository;
import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.common.exception.UnauthorizedActionException;
import org.example.backend.common.util.AggregateRows;
import org.example.backend.notification.entity.NotificationType;
import org.example.backend.notification.service.NotificationService;
import org.example.backend.user.entity.Client;
import org.example.backend.user.repository.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Avis publics des clients sur les annonces. Chaque écriture recalcule la note
 * moyenne dénormalisée de l'annonce (utilisée par la recherche et le tri).
 */
@Service
@RequiredArgsConstructor
public class AvisService {

    private final AvisAnnonceRepository avisRepository;
    private final AnnonceRepository annonceRepository;
    private final ClientRepository clientRepository;
    private final AvisMapper mapper;
    private final NotificationService notificationService;

    /** Crée l'avis du client sur l'annonce, ou met à jour celui qu'il a déjà laissé. */
    @Transactional
    public AvisDTO publier(Long clientId, Long annonceId, AvisRequest req) {
        Annonce annonce = annonceRepository.findById(annonceId)
                .filter(Annonce::isActive)
                .orElseThrow(() -> ResourceNotFoundException.of("Annonce", annonceId));
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> ResourceNotFoundException.of("Client", clientId));

        AvisAnnonce avis = avisRepository.findByAnnonceIdAndClientId(annonceId, clientId).orElse(null);
        boolean nouveau = avis == null;
        if (nouveau) {
            avis = AvisAnnonce.builder().annonce(annonce).client(client).build();
        }
        avis.setNote(req.note());
        avis.setCommentaire(StringUtils.hasText(req.commentaire()) ? req.commentaire().trim() : null);
        AvisAnnonce saved = avisRepository.saveAndFlush(avis);

        recalculerNote(annonce);

        if (nouveau) {
            notificationService.create(
                    annonce.getArtisan(),
                    NotificationType.NOUVEL_AVIS,
                    "Nouvel avis sur votre annonce",
                    "%s %s a noté « %s » %d/5"
                            .formatted(client.getPrenom(), client.getNom(), annonce.getTitre(), req.note()),
                    "/annonces/" + annonce.getId()
            );
        }
        return mapper.toDto(saved);
    }

    @Transactional
    public void supprimer(Long avisId, Long clientId) {
        AvisAnnonce avis = avisRepository.findById(avisId)
                .orElseThrow(() -> ResourceNotFoundException.of("Avis", avisId));
        if (!avis.getClient().getId().equals(clientId)) {
            throw new UnauthorizedActionException("Vous ne pouvez supprimer que vos propres avis");
        }
        Annonce annonce = avis.getAnnonce();
        avisRepository.delete(avis);
        avisRepository.flush();
        recalculerNote(annonce);
    }

    @Transactional(readOnly = true)
    public Page<AvisDTO> listerParAnnonce(Long annonceId, Pageable pageable) {
        return avisRepository.findByAnnonceId(annonceId, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<AvisDTO> listerParArtisan(Long artisanId, Pageable pageable) {
        return avisRepository.findByAnnonceArtisanId(artisanId, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<AvisDTO> mesAvis(Long clientId, Pageable pageable) {
        return avisRepository.findByClientId(clientId, pageable).map(mapper::toDto);
    }

    private void recalculerNote(Annonce annonce) {
        Object[] stats = avisRepository.statsForAnnonce(annonce.getId());
        annonce.setNoteMoyenne(BigDecimal.valueOf(AggregateRows.average(stats)).setScale(2, RoundingMode.HALF_UP));
        annonce.setNbAvis((int) AggregateRows.count(stats));
    }
}
