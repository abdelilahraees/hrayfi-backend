package org.example.backend.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.backend.user.entity.StatutValidation;

/** Validation, refus ou suspension d'un artisan ; le motif est montré à l'artisan à la connexion. */
public record ChangerStatutRequest(
        @NotNull StatutValidation statut,
        @Size(max = 1000) String motif
) {}
