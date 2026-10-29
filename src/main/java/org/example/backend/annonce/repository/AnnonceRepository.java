package org.example.backend.annonce.repository;

import org.example.backend.annonce.dto.MetierCountDTO;
import org.example.backend.annonce.entity.Annonce;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

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

    long countByArtisanId(Long artisanId);

    long countByArtisanIdAndActiveTrue(Long artisanId);

    @Query("""
           SELECT new org.example.backend.annonce.dto.MetierCountDTO(a.metier, COUNT(a))
           FROM Annonce a
           WHERE a.active = true
           GROUP BY a.metier
           ORDER BY COUNT(a) DESC
           """)
    List<MetierCountDTO> countByMetier();
}
