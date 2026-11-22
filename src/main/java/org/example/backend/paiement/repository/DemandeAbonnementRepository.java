package org.example.backend.paiement.repository;

import org.example.backend.paiement.entity.DemandeAbonnement;
import org.example.backend.paiement.entity.StatutDemandeAbonnement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DemandeAbonnementRepository extends JpaRepository<DemandeAbonnement, Long> {

    @EntityGraph(attributePaths = {"annonce", "paiement"})
    List<DemandeAbonnement> findByArtisanIdOrderByCreatedAtDesc(Long artisanId);

    boolean existsByArtisanIdAndStatutIn(Long artisanId, Collection<StatutDemandeAbonnement> statuts);

    long countByStatut(StatutDemandeAbonnement statut);

    /** Administration : demandes par statut (null = toutes), les plus anciennes en attente d'abord */
    @EntityGraph(attributePaths = {"artisan", "annonce", "traiteePar"})
    @Query("SELECT d FROM DemandeAbonnement d WHERE (:statut IS NULL OR d.statut = :statut)")
    Page<DemandeAbonnement> adminSearch(@Param("statut") StatutDemandeAbonnement statut, Pageable pageable);
}
