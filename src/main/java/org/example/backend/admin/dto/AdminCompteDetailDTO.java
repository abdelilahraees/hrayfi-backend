package org.example.backend.admin.dto;

import org.example.backend.paiement.dto.PaiementDTO;

import java.time.Instant;
import java.util.List;

/** Fiche complète d'un compte : historique des paiements et annonces (pour choisir laquelle mettre en avant). */
public record AdminCompteDetailDTO(
        AdminCompteDTO compte,
        List<PaiementDTO> paiements,
        List<AnnonceResume> annonces
) {
    public record AnnonceResume(Long id, String titre, boolean active, Instant sponsoriseJusquAu) {}
}
