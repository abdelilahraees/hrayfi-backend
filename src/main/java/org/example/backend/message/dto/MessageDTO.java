package org.example.backend.message.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MessageDTO(
        Long id,
        Long expediteurId,
        Long destinataireId,
        String contenu,
        Instant timestamp,
        Long annonceId,
        String annonceTitre,
        BigDecimal prixPropose,
        boolean lu
) {}
