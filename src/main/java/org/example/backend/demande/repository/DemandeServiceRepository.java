package org.example.backend.demande.repository;

import org.example.backend.demande.entity.DemandeService;
import org.example.backend.demande.entity.StatutDemande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DemandeServiceRepository extends JpaRepository<DemandeService, Long> {

    /**
     * EntityGraph : évite le N+1 en chargeant client + artisan en une seule requête
     * (JOIN FETCH automatique) au lieu d'une requête supplémentaire par demande.
     */
    @Override
    @EntityGraph(attributePaths = {"client", "artisan"})
    Optional<DemandeService> findById(Long id);

    @EntityGraph(attributePaths = {"client", "artisan"})
    Page<DemandeService> findByClientId(Long clientId, Pageable pageable);

    @EntityGraph(attributePaths = {"client", "artisan"})
    Page<DemandeService> findByArtisanId(Long artisanId, Pageable pageable);

    // --- requêtes dashboard ---

    long countByArtisanIdAndStatut(Long artisanId, StatutDemande statut);

    long countByClientIdAndStatut(Long clientId, StatutDemande statut);

    @Query("""
           SELECT COALESCE(SUM(d.prixFinal), 0)
           FROM DemandeService d
           WHERE d.artisan.id = :artisanId
             AND d.statut = org.example.backend.demande.entity.StatutDemande.TERMINEE
             AND d.updatedAt >= :debut
           """)
    BigDecimal chiffreAffairesArtisanDepuis(@Param("artisanId") Long artisanId,
                                            @Param("debut") Instant debut);

    @Query("""
           SELECT COALESCE(SUM(d.prixFinal), 0)
           FROM DemandeService d
           WHERE d.client.id = :clientId
             AND d.statut = org.example.backend.demande.entity.StatutDemande.TERMINEE
           """)
    BigDecimal totalDepenseClient(@Param("clientId") Long clientId);

    long countByArtisanIdAndStatutInAndUpdatedAtAfter(Long artisanId,
                                                      List<StatutDemande> statuts,
                                                      Instant debut);
}