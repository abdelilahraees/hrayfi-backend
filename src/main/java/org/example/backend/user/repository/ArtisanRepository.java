package org.example.backend.user.repository;

import org.example.backend.user.entity.Artisan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArtisanRepository extends JpaRepository<Artisan, Long> {
    List<Artisan> findByMetierIgnoreCaseAndVilleIgnoreCase(String metier, String ville);
    List<Artisan> findByMetierIgnoreCase(String metier);

    /** Recherche d'artisans : chaque filtre vide (chaîne "") est ignoré. */
    @Query("""
           SELECT a FROM Artisan a
           WHERE (:metier = '' OR LOWER(a.metier) = :metier)
             AND (:ville  = '' OR LOWER(a.ville)  = :ville)
             AND (:q      = '' OR LOWER(a.nom) LIKE :q OR LOWER(a.prenom) LIKE :q
                               OR LOWER(a.metier) LIKE :q OR LOWER(a.description) LIKE :q)
           """)
    Page<Artisan> search(@Param("metier") String metier,
                         @Param("ville") String ville,
                         @Param("q") String q,
                         Pageable pageable);
}
