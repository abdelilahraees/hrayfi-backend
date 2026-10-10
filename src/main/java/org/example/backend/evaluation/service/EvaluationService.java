package org.example.backend.evaluation.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.common.exception.UnauthorizedActionException;
import org.example.backend.demande.entity.DemandeService;
import org.example.backend.demande.entity.StatutDemande;
import org.example.backend.demande.repository.DemandeServiceRepository;
import org.example.backend.evaluation.dto.CreateEvaluationRequest;
import org.example.backend.evaluation.dto.EvaluationDTO;
import org.example.backend.evaluation.entity.Evaluation;
import org.example.backend.evaluation.mapper.EvaluationMapper;
import org.example.backend.evaluation.repository.EvaluationRepository;
import org.example.backend.notification.entity.NotificationType;
import org.example.backend.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final DemandeServiceRepository demandeRepository;
    private final EvaluationMapper mapper;
    private final NotificationService notificationService;

    /**
     * Règles métier :
     *   - seul le client propriétaire de la demande peut évaluer
     *   - la demande doit être TERMINEE
     *   - une seule évaluation par demande (unique DB constraint + check applicatif)
     */
    @Transactional
    public EvaluationDTO evaluer(Long clientId, Long demandeId, CreateEvaluationRequest req) {
        DemandeService demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> ResourceNotFoundException.of("Demande", demandeId));

        if (!demande.getClient().getId().equals(clientId)) {
            throw new UnauthorizedActionException("Vous ne pouvez évaluer que vos propres demandes");
        }
        if (demande.getStatut() != StatutDemande.TERMINEE) {
            throw new BusinessException("Seule une demande TERMINEE peut être évaluée (statut actuel : "
                    + demande.getStatut() + ")");
        }
        if (evaluationRepository.existsByDemandeId(demandeId)) {
            throw new BusinessException("Cette demande a déjà été évaluée");
        }

        Evaluation saved = evaluationRepository.save(Evaluation.builder()
                .demande(demande)
                .client(demande.getClient())
                .artisan(demande.getArtisan())
                .note(req.note())
                .commentaire(req.commentaire())
                .build());

        notificationService.create(
                demande.getArtisan(),
                NotificationType.NOUVELLE_EVALUATION,
                "Nouvelle évaluation reçue",
                "%s %s vous a attribué la note de %d/5"
                        .formatted(demande.getClient().getPrenom(), demande.getClient().getNom(), req.note()),
                "/evaluations/" + saved.getId()
        );

        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<EvaluationDTO> listerParArtisan(Long artisanId, Pageable pageable) {
        return evaluationRepository.findByArtisanId(artisanId, pageable).map(mapper::toDto);
    }
}
