package org.example.backend.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload entrant côté WebSocket (envoyé par le front sur /app/chat.send).
 */
public record ChatPayload(
        @NotNull Long destinataireId,
        @NotBlank @Size(max = 4000) String contenu
) {}
