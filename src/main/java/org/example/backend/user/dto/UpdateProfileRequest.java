package org.example.backend.user.dto;

import jakarta.validation.constraints.*;
import org.example.backend.user.entity.Disponibilite;

import java.math.BigDecimal;

/**
 * Mise à jour du profil connecté. Seuls les champs du rôle courant sont pris en compte ;
 * un champ null est laissé inchangé.
 */
public record UpdateProfileRequest(
        @Size(min = 1, max = 80) String nom,
        @Size(min = 1, max = 80) String prenom,
        @Size(max = 255) String adresse,
        @Size(max = 2000) String preferences,
        @Size(min = 2, max = 100) String metier,
        @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal tarifHoraire,
        @Size(max = 5000) String description,
        @Size(max = 100) String ville,
        Disponibilite disponibilite
) {}
