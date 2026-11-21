package org.example.backend.user.repository;

import org.example.backend.user.entity.Artisan;
import org.example.backend.user.entity.StatutValidation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
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

    /**
     * Artisans validés les plus proches d'un point (formule de Haversine, rayon terrestre 6371 km).
     * Chaque ligne : [id, distance en km]. Métier vide = tous les métiers.
     */
    @Query(value = """
           SELECT t.id, t.distance FROM (
               SELECT a.id AS id,
                      6371 * acos(least(1.0, cos(radians(:lat)) * cos(radians(a.latitude)) * cos(radians(a.longitude) - radians(:lng))
                                             + sin(radians(:lat)) * sin(radians(a.latitude)))) AS distance
               FROM artisan a
               WHERE a.statut_validation = 'VALIDE'
                 AND a.latitude IS NOT NULL AND a.longitude IS NOT NULL
                 AND (:metier = '' OR LOWER(a.metier) = :metier)
           ) t
           WHERE t.distance <= :rayon
           ORDER BY t.distance
           LIMIT :limite
           """, nativeQuery = true)
    List<Object[]> proches(@Param("lat") double lat, @Param("lng") double lng, @Param("rayon") double rayonKm,
                           @Param("metier") String metier, @Param("limite") int limite);

    List<Artisan> findByLatitudeIsNull();

    /**
     * Comptes des artisans validés pour le suivi des paiements. filtre : TOUS, A_RELANCER (échéance passée
     * ou dans moins de 15 jours), PAYES, OFFERTS, EXPIRES.
     */
    @Query("""
           SELECT a FROM Artisan a
           WHERE a.statutValidation = org.example.backend.user.entity.StatutValidation.VALIDE
             AND (:q = '' OR LOWER(a.email) LIKE :q OR LOWER(a.nom) LIKE :q OR LOWER(a.prenom) LIKE :q
                          OR LOWER(a.metier) LIKE :q OR LOWER(a.ville) LIKE :q OR a.telephone LIKE :q)
             AND (:filtre = 'TOUS'
                  OR (:filtre = 'A_RELANCER' AND a.proJusquAu < :bientot)
                  OR (:filtre = 'PAYES'      AND a.proOffert = false AND a.proJusquAu >= :maintenant)
                  OR (:filtre = 'OFFERTS'    AND a.proOffert = true  AND a.proJusquAu >= :maintenant)
                  OR (:filtre = 'EXPIRES'    AND a.proJusquAu < :maintenant))
           """)
    Page<Artisan> comptes(@Param("filtre") String filtre, @Param("q") String q,
                          @Param("maintenant") Instant maintenant, @Param("bientot") Instant bientot, Pageable pageable);

    @Query("""
           SELECT COUNT(a) FROM Artisan a
           WHERE a.statutValidation = org.example.backend.user.entity.StatutValidation.VALIDE
             AND a.proJusquAu < :bientot
           """)
    long countARelancer(@Param("bientot") Instant bientot);

    long countByStatutValidationAndProOffertAndProJusquAuAfter(StatutValidation statut, boolean offert, Instant date);
}
