package org.example.backend.annonce.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Création / modification d'une annonce. Le métier et la ville prennent par défaut
 * ceux du profil artisan s'ils ne sont pas fournis.
 */
public record AnnonceRequest(
        @NotBlank @Size(min = 5, max = 150) String titre,
        @NotBlank @Size(min = 20, max = 5000) String description,
        @Size(max = 100) String metier,
        @Size(max = 100) String ville,
        @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal prixMin,
        @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal prixMax
) {}
