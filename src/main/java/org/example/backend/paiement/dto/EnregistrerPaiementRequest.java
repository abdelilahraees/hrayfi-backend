package org.example.backend.paiement.dto;

import jakarta.validation.constraints.*;
import org.example.backend.paiement.entity.MoyenPaiement;
import org.example.backend.paiement.entity.ObjetPaiement;

import java.math.BigDecimal;

/** Paiement encaissé par l'équipe ; annonceId obligatoire pour une mise en avant. */
public record EnregistrerPaiementRequest(
        @NotNull ObjetPaiement objet,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal montant,
        @NotNull MoyenPaiement moyen,
        @Size(max = 100) String reference,
        @NotNull @Min(1) @Max(730) Integer dureeJours,
        Long annonceId,
        @Size(max = 2000) String note
) {}
