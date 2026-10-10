package org.example.backend.auth.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RegisterArtisanRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank String nom,
        @NotBlank String prenom,
        @NotBlank String metier,
        @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal tarifHoraire,
        String description,
        String ville
) {}
