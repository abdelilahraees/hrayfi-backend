package org.example.backend.annonce.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AnnonceDTO(
        Long id,
        Long artisanId,
        String artisanNomComplet,
        String titre,
        String description,
        String metier,
        String ville,
        BigDecimal prixMin,
        BigDecimal prixMax,
        boolean active,
        double noteMoyenne,
        int nbAvis,
        List<ImageDTO> images,
        Instant createdAt,
        Instant updatedAt
) {
    public record ImageDTO(Long id, String url) {}
}
