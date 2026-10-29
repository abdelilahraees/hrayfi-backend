package org.example.backend.dashboard.dto;

import org.example.backend.demande.dto.DemandeServiceDTO;

import java.math.BigDecimal;
import java.util.List;

public record ClientDashboardDTO(
        long demandesEnAttente,
        long demandesAcceptees,
        long demandesTermineesTotal,
        BigDecimal totalDepense,
        long nbAvisDonnes,
        List<DemandeServiceDTO> dernieresDemandes
) {}