package org.example.backend.demande.dto;

import org.example.backend.demande.entity.StatutDemande;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Vue API d'une demande. On aplatit volontairement client/artisan en couples
 * (id, nomComplet) pour éviter d'exposer les entités et prévenir les cycles JSON.
 */
public record DemandeServiceDTO(
        Long id,
        Long clientId,
        String clientNomComplet,
        Long artisanId,
        String artisanNomComplet,
        String description,
        Instant dateIntervention,
        StatutDemande statut,
        BigDecimal prixFinal,
        Instant createdAt,
        Instant updatedAt
) {}