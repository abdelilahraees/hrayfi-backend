package org.example.backend.admin.dto;

import org.example.backend.user.entity.StatutValidation;

import java.math.BigDecimal;
import java.time.Instant;

/** Fiche artisan vue par l'administrateur, avec son statut de validation. */
public record AdminArtisanDTO(
        Long id,
        String prenom,
        String nom,
        String email,
        String metier,
        String ville,
        BigDecimal tarifHoraire,
        String description,
        Instant createdAt,
        StatutValidation statutValidation,
        String motifRefus,
        Instant valideLe,
        long nbAnnonces
) {}
