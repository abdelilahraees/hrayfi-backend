package org.example.backend.message.dto;

import java.time.Instant;

public record MessageDTO(
        Long id,
        Long expediteurId,
        Long destinataireId,
        String contenu,
        Instant timestamp
) {}
