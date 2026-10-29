package org.example.backend.dashboard.dto;

import org.example.backend.demande.dto.DemandeServiceDTO;

import java.math.BigDecimal;
import java.util.List;

public record ArtisanDashboardDTO(
        long demandesEnAttente,
        long demandesAcceptees,
        long demandesTermineesCeMois,
        BigDecimal chiffreAffairesCeMois,
        double noteMoyenne,
        long nbEvaluations,
        double tauxAcceptation,            // 0.0 à 1.0 ; 0 si pas d'historique
        long nbAnnonces,
        long nbAnnoncesActives,
        double noteMoyenneAnnonces,        // moyenne des avis laissés sur ses annonces
        long nbAvisAnnonces,
        List<DemandeServiceDTO> prochainesInterventions
) {}