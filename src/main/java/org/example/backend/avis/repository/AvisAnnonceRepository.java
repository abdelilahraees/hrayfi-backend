package org.example.backend.avis.repository;

import org.example.backend.avis.entity.AvisAnnonce;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AvisAnnonceRepository extends JpaRepository<AvisAnnonce, Long> {

    @Override
    @EntityGraph(attributePaths = {"annonce", "client"})
    Optional<AvisAnnonce> findById(Long id);

    long countByClientId(Long clientId);

    Optional<AvisAnnonce> findByAnnonceIdAndClientId(Long annonceId, Long clientId);

    @EntityGraph(attributePaths = {"annonce", "client"})
    Page<AvisAnnonce> findByAnnonceId(Long annonceId, Pageable pageable);

    @EntityGraph(attributePaths = {"annonce", "client"})
    Page<AvisAnnonce> findByClientId(Long clientId, Pageable pageable);

    @EntityGraph(attributePaths = {"annonce", "client"})
    Page<AvisAnnonce> findByAnnonceArtisanId(Long artisanId, Pageable pageable);

    /** (moyenne, nombre) des avis d'une annonce. */
    @Query("""
           SELECT COALESCE(AVG(a.note), 0.0), COUNT(a)
           FROM AvisAnnonce a
           WHERE a.annonce.id = :annonceId
           """)
    Object[] statsForAnnonce(@Param("annonceId") Long annonceId);

    /** (moyenne, nombre) de tous les avis reçus sur les annonces d'un artisan. */
    @Query("""
           SELECT COALESCE(AVG(a.note), 0.0), COUNT(a)
           FROM AvisAnnonce a
           WHERE a.annonce.artisan.id = :artisanId
           """)
    Object[] statsForArtisan(@Param("artisanId") Long artisanId);
}
