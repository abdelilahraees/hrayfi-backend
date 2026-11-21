package org.example.backend.user.repository;

import org.example.backend.user.entity.StatutValidation;
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

    /** Recherche d'artisans validés : chaque filtre vide (chaîne "") est ignoré. */
    @Query("""
           SELECT a FROM Artisan a
           WHERE a.statutValidation = org.example.backend.user.entity.StatutValidation.VALIDE
             AND (:metier = '' OR LOWER(a.metier) = :metier)
             AND (:ville  = '' OR LOWER(a.ville)  = :ville)
             AND (:q      = '' OR LOWER(a.nom) LIKE :q OR LOWER(a.prenom) LIKE :q
                               OR LOWER(a.metier) LIKE :q OR LOWER(a.description) LIKE :q)
           """)
    Page<Artisan> search(@Param("metier") String metier,
                         @Param("ville") String ville,
                         @Param("q") String q,
                         Pageable pageable);

    long countByStatutValidation(StatutValidation statut);

    /** Administration : artisans par statut de validation (null = tous) et texte libre. */
    @Query("""
           SELECT a FROM Artisan a
           WHERE (:statut IS NULL OR a.statutValidation = :statut)
             AND (:q = '' OR LOWER(a.email) LIKE :q OR LOWER(a.nom) LIKE :q OR LOWER(a.prenom) LIKE :q
                          OR LOWER(a.metier) LIKE :q OR LOWER(a.ville) LIKE :q)
           """)
    Page<Artisan> adminSearch(@Param("statut") StatutValidation statut, @Param("q") String q, Pageable pageable);
}
