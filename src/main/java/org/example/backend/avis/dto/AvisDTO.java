package org.example.backend.avis.dto;

import java.time.Instant;

public record AvisDTO(
        Long id,
        Long annonceId,
        String annonceTitre,
        Long clientId,
        String clientNomComplet,
        Integer note,
        String commentaire,
        Instant createdAt,
        Instant updatedAt,
        /** L'auteur a eu une intervention terminée avec cet artisan via Hrayfi */
        boolean verifie
) {
    public AvisDTO avecVerifie(boolean v) {
        return new AvisDTO(id, annonceId, annonceTitre, clientId, clientNomComplet, note, commentaire, createdAt, updatedAt, v);
    }
}
