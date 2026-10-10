package org.example.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binding type-safe des propriétés JWT depuis application.yml (préfixe app.security.jwt).
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secret,
        long expirationMs,
        String issuer
) {}
