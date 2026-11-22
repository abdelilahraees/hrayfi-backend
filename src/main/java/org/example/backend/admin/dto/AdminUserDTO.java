package org.example.backend.admin.dto;

import org.example.backend.user.entity.Role;
import org.example.backend.user.entity.StatutValidation;

import java.time.Instant;

/** Compte utilisateur (tous rôles) ; statutValidation n'est renseigné que pour les artisans. */
public record AdminUserDTO(
        Long id,
        String prenom,
        String nom,
        String email,
        Role role,
        Instant createdAt,
        StatutValidation statutValidation
) {}
