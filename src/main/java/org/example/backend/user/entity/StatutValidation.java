package org.example.backend.user.entity;

/** Cycle de vie d'un compte artisan : seul un compte VALIDE peut se connecter et être visible publiquement. */
public enum StatutValidation {
    EN_ATTENTE,
    VALIDE,
    REFUSE,
    SUSPENDU
}
