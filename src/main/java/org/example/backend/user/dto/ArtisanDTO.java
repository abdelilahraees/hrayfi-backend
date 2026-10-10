package org.example.backend.user.dto;

import org.example.backend.user.entity.Disponibilite;
import org.example.backend.user.entity.Role;

import java.math.BigDecimal;
import java.time.Instant;

public record ArtisanDTO(
        Long id,
        String email,
        String nom,
        String prenom,
        Role role,
        String metier,
        BigDecimal tarifHoraire,
        String description,
        String ville,
        Disponibilite disponibilite,
        Instant createdAt
) {}