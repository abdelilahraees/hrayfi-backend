package org.example.backend.paiement.dto;

import org.example.backend.user.entity.StatutPro;

import java.time.Instant;
import java.util.List;

/** « Mon offre » côté artisan : situation Pro, annonces mises en avant et paiements enregistrés. */
public record OffreDTO(
        StatutPro statutPro,
        Instant proJusquAu,
        boolean proOffert,
        List<AnnonceMiseEnAvant> misesEnAvant,
        List<PaiementDTO> paiements,
        /** Une demande d'offre attend la réponse de l'équipe */
        boolean demandeEnCours
) {
    public record AnnonceMiseEnAvant(Long id, String titre, Instant jusquAu) {}
}
