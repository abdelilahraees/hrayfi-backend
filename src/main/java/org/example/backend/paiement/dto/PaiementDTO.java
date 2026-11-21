package org.example.backend.paiement.dto;

import org.example.backend.paiement.entity.MoyenPaiement;
import org.example.backend.paiement.entity.ObjetPaiement;

import java.math.BigDecimal;
import java.time.Instant;

public record PaiementDTO(
        Long id,
        ObjetPaiement objet,
        BigDecimal montant,
        MoyenPaiement moyen,
        String reference,
        int dureeJours,
        Long annonceId,
        String annonceTitre,
        String note,
        String enregistrePar,
        Instant createdAt
) {}
