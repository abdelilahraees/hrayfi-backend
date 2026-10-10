package org.example.backend.user.dto;

import org.example.backend.user.entity.Role;

import java.time.Instant;

public record ClientDTO(
        Long id,
        String email,
        String nom,
        String prenom,
        Role role,
        String adresse,
        String preferences,
        Instant createdAt
) {}
