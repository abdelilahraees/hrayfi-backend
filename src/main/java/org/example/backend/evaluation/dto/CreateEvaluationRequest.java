package org.example.backend.evaluation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEvaluationRequest(
        @NotNull @Min(1) @Max(5) Integer note,
        @Size(max = 2000) String commentaire
) {}