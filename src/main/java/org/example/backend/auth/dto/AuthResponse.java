package org.example.backend.auth.dto;

import org.example.backend.user.entity.Role;
import org.example.backend.user.entity.StatutValidation;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInMs,
        Long userId,
        String email,
        Role role,
        /** Renseigné pour un artisan ; sans jeton tant que le compte n'est pas VALIDE */
        StatutValidation statutValidation
) {}
