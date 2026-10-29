package org.example.backend.message.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Message entrant, envoyé en REST (POST /api/messages) ou en WebSocket (/app/chat.send).
 * annonceId et prixPropose sont optionnels : ensemble ils forment une offre de prix.
 */
public record ChatPayload(
        @NotNull Long destinataireId,
        @NotBlank @Size(max = 4000) String contenu,
        Long annonceId,
        @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal prixPropose
) {}
