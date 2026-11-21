package org.example.backend.admin.dto;

import org.example.backend.user.entity.StatutPro;

import java.math.BigDecimal;
import java.time.Instant;

/** Compte d'un artisan validé vu par l'équipe : offre Pro, paiements, mise en avant, notes de suivi. */
public record AdminCompteDTO(
        Long id,
        String prenom,
        String nom,
        String email,
        String telephone,
        String metier,
        String ville,
        StatutPro statutPro,
        Instant proJusquAu,
        boolean proOffert,
        BigDecimal totalPaye,
        long nbAnnonces,
        long annoncesMisesEnAvant,
        String noteAdmin,
        Instant createdAt
) {}
