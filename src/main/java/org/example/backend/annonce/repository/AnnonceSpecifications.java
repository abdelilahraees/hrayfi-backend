package org.example.backend.annonce.repository;

import org.example.backend.annonce.dto.AnnonceSearchCriteria;
import org.example.backend.annonce.entity.Annonce;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * Construit dynamiquement la clause WHERE de la recherche d'annonces :
 * chaque filtre non renseigné est simplement ignoré.
 */
public final class AnnonceSpecifications {

    private AnnonceSpecifications() {}

    public static Specification<Annonce> search(AnnonceSearchCriteria c) {
        return Specification.where(active())
                .and(keyword(c.q()))
                .and(equalsIgnoreCase("metier", c.metier()))
                .and(equalsIgnoreCase("ville", c.ville()))
                .and(noteMin(c.noteMin()))
                .and(budget(c.prixMax()));
    }

    private static Specification<Annonce> active() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    private static Specification<Annonce> keyword(String q) {
        if (!StringUtils.hasText(q)) return null;
        String pattern = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("titre")), pattern),
                cb.like(cb.lower(root.get("description")), pattern),
                cb.like(cb.lower(root.get("metier")), pattern)
        );
    }

    private static Specification<Annonce> equalsIgnoreCase(String field, String value) {
        if (!StringUtils.hasText(value)) return null;
        return (root, query, cb) -> cb.equal(cb.lower(root.get(field)), value.trim().toLowerCase());
    }

    private static Specification<Annonce> noteMin(Double noteMin) {
        if (noteMin == null || noteMin <= 0) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("noteMoyenne"), BigDecimal.valueOf(noteMin));
    }

    private static Specification<Annonce> budget(BigDecimal prixMax) {
        if (prixMax == null) return null;
        return (root, query, cb) -> cb.or(
                cb.isNull(root.get("prixMin")),
                cb.lessThanOrEqualTo(root.get("prixMin"), prixMax)
        );
    }
}
