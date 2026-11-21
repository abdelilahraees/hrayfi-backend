package org.example.backend.user.entity;

/** Situation de l'offre Pro d'un artisan, pour le suivi des paiements par l'équipe. */
public enum StatutPro {
    /** Pas encore validé : pas d'offre */
    AUCUN,
    /** Période offerte en cours */
    OFFERT,
    /** Abonnement payé en cours */
    PAYE,
    /** Échéance passée : à relancer (l'artisan garde l'accès) */
    EXPIRE
}
