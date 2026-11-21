package org.example.backend.user.dto;

import org.example.backend.user.entity.Disponibilite;
import org.example.backend.user.entity.Role;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Profil de l'utilisateur connecté. Les champs propres à l'autre rôle restent null
 * (adresse/preferences pour un artisan, metier/tarif/... pour un client).
 */
public record MeDTO(
        Long id,
        String email,
        String nom,
        String prenom,
        Role role,
        String telephone,
        Instant createdAt,
        String adresse,
        String preferences,
        String metier,
        BigDecimal tarifHoraire,
        String description,
        String ville,
        Disponibilite disponibilite,
        Double latitude,
        Double longitude,
        /** true : centre de la ville déclarée, pas encore précisé sur la carte */
        Boolean positionApprox
) {}
