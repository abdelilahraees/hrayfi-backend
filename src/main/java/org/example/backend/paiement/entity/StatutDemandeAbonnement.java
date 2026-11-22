package org.example.backend.paiement.entity;

public enum StatutDemandeAbonnement {
    /** Envoyée par l'artisan, pas encore traitée */
    EN_ATTENTE,
    /** L'équipe a contacté l'artisan, paiement en attente */
    CONTACTEE,
    /** Paiement reçu et enregistré : l'offre est active */
    PAYEE,
    /** Refusée par l'équipe (avec une réponse) */
    REFUSEE,
    /** Retirée par l'artisan */
    ANNULEE
}
