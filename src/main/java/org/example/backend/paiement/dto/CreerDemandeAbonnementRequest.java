package org.example.backend.paiement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.backend.paiement.entity.Formule;
import org.example.backend.paiement.entity.MoyenPaiement;

/** Demande d'offre envoyée par l'artisan ; annonceId obligatoire pour une mise en avant. */
public record CreerDemandeAbonnementRequest(
        @NotNull Formule formule,
        Long annonceId,
        @NotNull MoyenPaiement moyenSouhaite,
        @Size(max = 1000) String message
) {}
