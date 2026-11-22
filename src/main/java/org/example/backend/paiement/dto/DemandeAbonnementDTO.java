package org.example.backend.paiement.dto;

import org.example.backend.paiement.entity.MoyenPaiement;
import org.example.backend.paiement.entity.StatutDemandeAbonnement;
import org.example.backend.user.entity.StatutPro;
import org.example.backend.user.entity.StatutValidation;

import java.time.Instant;

public record DemandeAbonnementDTO(
        Long id,
        Long artisanId,
        String artisanNomComplet,
        String artisanTelephone,
        String artisanMetier,
        String artisanVille,
        StatutValidation artisanStatut,
        StatutPro statutPro,
        Instant proJusquAu,
        FormuleDTO formule,
        Long annonceId,
        String annonceTitre,
        MoyenPaiement moyenSouhaite,
        String message,
        StatutDemandeAbonnement statut,
        String reponseAdmin,
        String traiteePar,
        Instant traiteeLe,
        Instant createdAt
) {}
