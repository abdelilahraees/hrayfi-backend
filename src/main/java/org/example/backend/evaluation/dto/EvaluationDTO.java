package org.example.backend.evaluation.dto;

import java.time.Instant;

public record EvaluationDTO(
        Long id,
        Long demandeId,
        Long clientId,
        String clientNomComplet,
        Long artisanId,
        Integer note,
        String commentaire,
        Instant createdAt
) {}