package org.example.backend.auth.dto;

import org.example.backend.user.entity.Role;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInMs,
        Long userId,
        String email,
        Role role
) {}
