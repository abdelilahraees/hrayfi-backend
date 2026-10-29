package org.example.backend.avis.dto;

import java.time.Instant;

public record AvisDTO(
        Long id,
        Long annonceId,
        String annonceTitre,
        Long clientId,
        String clientNomComplet,
        Integer note,
        String commentaire,
        Instant createdAt,
        Instant updatedAt
) {}
