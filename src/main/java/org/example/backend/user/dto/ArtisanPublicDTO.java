package org.example.backend.user.dto;

import org.example.backend.user.entity.Disponibilite;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Profil public d'un artisan (sans e-mail) enrichi de sa réputation :
 * moyenne et nombre des avis reçus sur l'ensemble de ses annonces.
 */
public record ArtisanPublicDTO(
        Long id,
        String nom,
        String prenom,
        String metier,
        BigDecimal tarifHoraire,
        String description,
        String ville,
        Disponibilite disponibilite,
        double noteMoyenne,
        long nbAvis,
        long nbAnnonces,
        Instant createdAt
) {}
