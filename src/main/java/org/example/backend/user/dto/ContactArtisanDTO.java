package org.example.backend.user.dto;

/** Numéro d'un artisan validé, réservé aux utilisateurs connectés (bouton WhatsApp / appel). */
public record ContactArtisanDTO(Long artisanId, String telephone) {}
