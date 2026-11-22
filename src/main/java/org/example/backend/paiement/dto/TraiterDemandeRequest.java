package org.example.backend.paiement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.backend.paiement.entity.MoyenPaiement;

/**
 * Décision de l'équipe sur une demande d'offre. PAYEE enregistre le paiement (moyen et référence facultatifs,
 * sinon le moyen souhaité). REFUSEE et SUSPENDRE_COMPTE exigent une réponse, montrée à l'artisan.
 */
public record TraiterDemandeRequest(
        @NotNull Decision decision,
        @Size(max = 2000) String reponse,
        MoyenPaiement moyen,
        @Size(max = 100) String reference
) {
    public enum Decision { CONTACTEE, PAYEE, REFUSEE, SUSPENDRE_COMPTE }
}
