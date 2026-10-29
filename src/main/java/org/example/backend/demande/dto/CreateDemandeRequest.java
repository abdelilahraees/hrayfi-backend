package org.example.backend.demande.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateDemandeRequest(
        @NotNull Long artisanId,
        @NotBlank @Size(min = 10, max = 2000) String description,
        @NotNull @Future Instant dateIntervention,
        Long annonceId,
        @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal prixConvenu
) {}