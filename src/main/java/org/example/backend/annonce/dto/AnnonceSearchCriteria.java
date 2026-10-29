package org.example.backend.annonce.dto;

import java.math.BigDecimal;

/**
 * Filtres de recherche publique. Tous optionnels.
 *
 * @param q        mot-clé cherché dans le titre et la description
 * @param metier   métier exact (insensible à la casse)
 * @param ville    ville exacte (insensible à la casse)
 * @param noteMin  note moyenne minimale (1 à 5)
 * @param prixMax  budget maximal : garde les annonces dont le prix minimum est ≤ budget
 */
public record AnnonceSearchCriteria(
        String q,
        String metier,
        String ville,
        Double noteMin,
        BigDecimal prixMax
) {}
