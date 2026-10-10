package org.example.backend.evaluation.repository;

import org.example.backend.evaluation.entity.Evaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    boolean existsByDemandeId(Long demandeId);

    @EntityGraph(attributePaths = {"client", "artisan"})
    Page<Evaluation> findByArtisanId(Long artisanId, Pageable pageable);

    /**
     * Projection agrégée utilisée par le dashboard et par ArtisanDTO enrichi.
     * Retourne (moyenne, nombre). Les deux valeurs sont 0 si l'artisan n'a aucune éval.
     */
    @Query("""
           SELECT COALESCE(AVG(e.note), 0.0), COUNT(e)
           FROM Evaluation e
           WHERE e.artisan.id = :artisanId
           """)
    Object[] statsForArtisan(@Param("artisanId") Long artisanId);
}