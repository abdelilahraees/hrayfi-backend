package org.example.backend.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.dashboard.dto.ArtisanDashboardDTO;
import org.example.backend.dashboard.dto.ClientDashboardDTO;
import org.example.backend.demande.dto.DemandeServiceDTO;
import org.example.backend.demande.entity.StatutDemande;
import org.example.backend.demande.mapper.DemandeServiceMapper;
import org.example.backend.demande.repository.DemandeServiceRepository;
import org.example.backend.evaluation.repository.EvaluationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

/**
 * Agrégats pré-calculés pour les écrans "Mon tableau de bord".
 * Un seul endpoint = un seul round-trip côté front.
 * Les requêtes sont de type COUNT/SUM/AVG — pas de chargement d'entités en bulk.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DemandeServiceRepository demandeRepository;
    private final EvaluationRepository evaluationRepository;
    private final DemandeServiceMapper demandeMapper;

    @Transactional(readOnly = true)
    public ArtisanDashboardDTO dashboardArtisan(Long artisanId) {
        Instant debutMois = debutDuMoisCourant();

        long enAttente   = demandeRepository.countByArtisanIdAndStatut(artisanId, StatutDemande.EN_ATTENTE);
        long acceptees   = demandeRepository.countByArtisanIdAndStatut(artisanId, StatutDemande.ACCEPTEE);
        long termineesCM = demandeRepository.countByArtisanIdAndStatutInAndUpdatedAtAfter(
                artisanId, List.of(StatutDemande.TERMINEE), debutMois);
        long refusees    = demandeRepository.countByArtisanIdAndStatut(artisanId, StatutDemande.REFUSEE);
        long termineesTotal = demandeRepository.countByArtisanIdAndStatut(artisanId, StatutDemande.TERMINEE);

        BigDecimal caMois = demandeRepository.chiffreAffairesArtisanDepuis(artisanId, debutMois);

        Object[] stats = evaluationRepository.statsForArtisan(artisanId);
        double noteMoyenne = stats != null && stats[0] != null ? ((Number) stats[0]).doubleValue() : 0.0;
        long nbEval        = stats != null && stats[1] != null ? ((Number) stats[1]).longValue()   : 0L;

        long totalDecisions = acceptees + termineesTotal + refusees;
        double tauxAcceptation = totalDecisions == 0
                ? 0.0
                : (double) (acceptees + termineesTotal) / totalDecisions;

        List<DemandeServiceDTO> prochaines = demandeRepository.findByArtisanId(
                        artisanId,
                        PageRequest.of(0, 5, Sort.by("dateIntervention").ascending()))
                .stream()
                .filter(d -> d.getStatut() == StatutDemande.ACCEPTEE)
                .map(demandeMapper::toDto)
                .toList();

        return new ArtisanDashboardDTO(
                enAttente, acceptees, termineesCM, caMois,
                round2(noteMoyenne), nbEval, round2(tauxAcceptation),
                prochaines
        );
    }

    @Transactional(readOnly = true)
    public ClientDashboardDTO dashboardClient(Long clientId) {
        long enAttente  = demandeRepository.countByClientIdAndStatut(clientId, StatutDemande.EN_ATTENTE);
        long acceptees  = demandeRepository.countByClientIdAndStatut(clientId, StatutDemande.ACCEPTEE);
        long termineesT = demandeRepository.countByClientIdAndStatut(clientId, StatutDemande.TERMINEE);
        BigDecimal depense = demandeRepository.totalDepenseClient(clientId);

        List<DemandeServiceDTO> dernieres = demandeRepository.findByClientId(
                        clientId,
                        PageRequest.of(0, 5, Sort.by("createdAt").descending()))
                .map(demandeMapper::toDto)
                .getContent();

        return new ClientDashboardDTO(enAttente, acceptees, termineesT, depense, dernieres);
    }

    private static Instant debutDuMoisCourant() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        return now.withDayOfMonth(1).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
