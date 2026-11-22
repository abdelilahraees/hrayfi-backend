package org.example.backend.paiement.dto;

import org.example.backend.paiement.entity.Formule;
import org.example.backend.paiement.entity.ObjetPaiement;

import java.math.BigDecimal;
import java.util.List;

/** Offre du catalogue avec ce qu'elle apporte, telle qu'affichée à l'artisan et à l'équipe. */
public record FormuleDTO(Formule code, ObjetPaiement objet, String libelle, BigDecimal montant, int dureeJours, List<String> avantages) {

    public static FormuleDTO of(Formule f) {
        return new FormuleDTO(f, f.objet(), f.libelle(), f.montant(), f.dureeJours(), f.avantages());
    }
}
