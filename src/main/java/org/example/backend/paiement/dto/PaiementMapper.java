package org.example.backend.paiement.dto;

import org.example.backend.paiement.entity.Paiement;

public final class PaiementMapper {

    private PaiementMapper() {}

    public static PaiementDTO toDto(Paiement p) {
        return new PaiementDTO(
                p.getId(), p.getObjet(), p.getMontant(), p.getMoyen(), p.getReference(), p.getDureeJours(),
                p.getAnnonce() == null ? null : p.getAnnonce().getId(),
                p.getAnnonce() == null ? null : p.getAnnonce().getTitre(),
                p.getNote(),
                p.getEnregistrePar() == null ? null : (p.getEnregistrePar().getPrenom() + " " + p.getEnregistrePar().getNom()).trim(),
                p.getCreatedAt());
    }
}
