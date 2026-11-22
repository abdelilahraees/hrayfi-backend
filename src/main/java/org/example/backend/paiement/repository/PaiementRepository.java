package org.example.backend.paiement.repository;

import org.example.backend.paiement.entity.Paiement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    @EntityGraph(attributePaths = {"annonce", "enregistrePar"})
    List<Paiement> findByArtisanIdOrderByCreatedAtDesc(Long artisanId);

    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Paiement p WHERE p.createdAt >= :depuis")
    BigDecimal totalDepuis(@Param("depuis") Instant depuis);

    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Paiement p WHERE p.artisan.id = :artisanId")
    BigDecimal totalArtisan(@Param("artisanId") Long artisanId);

    /** Revenus par mois sur une période : [année, mois, total] */
    @Query("""
           SELECT YEAR(p.createdAt), MONTH(p.createdAt), SUM(p.montant) FROM Paiement p
           WHERE p.createdAt >= :depuis
           GROUP BY YEAR(p.createdAt), MONTH(p.createdAt)
           """)
    List<Object[]> revenusParMois(@Param("depuis") Instant depuis);
}
