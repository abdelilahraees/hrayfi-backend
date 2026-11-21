package org.example.backend.admin.dto;

import java.math.BigDecimal;
import java.util.Map;

/** Chiffres clés de la plateforme pour le tableau de bord administrateur. */
public record AdminStatsDTO(
        long clients,
        long artisans,
        Map<String, Long> artisansParStatut,
        long annonces,
        long annoncesEnLigne,
        long demandes,
        Map<String, Long> demandesParStatut,
        long avis,
        double noteMoyenne,
        BigDecimal volumeAffaires
) {}
