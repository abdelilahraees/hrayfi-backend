package org.example.backend.admin.dto;

import java.math.BigDecimal;
import java.util.List;
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
        BigDecimal volumeAffaires,
        // ---- revenus de la plateforme (paiements encaissés hors ligne) ----
        BigDecimal revenusMois,
        BigDecimal revenusTotal,
        long prosPayants,
        long prosOfferts,
        long aRelancer,
        long annoncesMisesEnAvant,
        /** 6 derniers mois, du plus ancien au plus récent */
        List<RevenuMois> revenusParMois,
        /** Demandes d'offre envoyées par les artisans, à traiter */
        long demandesOffreEnAttente
) {
    public record RevenuMois(int annee, int mois, BigDecimal total) {}
}
