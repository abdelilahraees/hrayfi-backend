package org.example.backend.annonce.repository;

import org.example.backend.annonce.dto.MetierCountDTO;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.user.entity.StatutValidation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AnnonceRepository extends JpaRepository<Annonce, Long>, JpaSpecificationExecutor<Annonce> {

    @Override
    @EntityGraph(attributePaths = {"artisan"})
    Optional<Annonce> findById(Long id);

    // Les images (collection) sont chargées par lot grâce à default_batch_fetch_size
    @Override
    @EntityGraph(attributePaths = {"artisan"})
    Page<Annonce> findAll(Specification<Annonce> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"artisan"})
    Page<Annonce> findByArtisanId(Long artisanId, Pageable pageable);

    @EntityGraph(attributePaths = {"artisan"})
    Page<Annonce> findByArtisanIdAndActiveTrue(Long artisanId, Pageable pageable);

    @EntityGraph(attributePaths = {"artisan"})
    Page<Annonce> findByArtisanIdAndActiveTrueAndArtisanStatutValidation(Long artisanId, StatutValidation statut, Pageable pageable);

    long countByArtisanId(Long artisanId);

    long countByArtisanIdAndActiveTrue(Long artisanId);

    @Query("""
           SELECT new org.example.backend.annonce.dto.MetierCountDTO(a.metier, COUNT(a))
           FROM Annonce a
           WHERE a.active = true
             AND a.artisan.statutValidation = org.example.backend.user.entity.StatutValidation.VALIDE
           GROUP BY a.metier
           ORDER BY COUNT(a) DESC
           """)
    List<MetierCountDTO> countByMetier();

    long countByActiveTrue();

    boolean existsByArtisanIdAndActiveTrueAndSponsoriseJusquAuAfter(Long artisanId, Instant date);

    long countBySponsoriseJusquAuAfter(Instant date);

    List<Annonce> findByArtisanIdOrderByCreatedAtDesc(Long artisanId);

    /** Administration : toutes les annonces (en ligne ou masquées), quel que soit le statut de l'artisan. */
    @EntityGraph(attributePaths = {"artisan"})
    @Query("""
           SELECT a FROM Annonce a
           WHERE (:q = '' OR LOWER(a.titre) LIKE :q OR LOWER(a.metier) LIKE :q OR LOWER(a.ville) LIKE :q
                          OR LOWER(a.artisan.nom) LIKE :q OR LOWER(a.artisan.prenom) LIKE :q)
           """)
    Page<Annonce> adminSearch(@Param("q") String q, Pageable pageable);
}
