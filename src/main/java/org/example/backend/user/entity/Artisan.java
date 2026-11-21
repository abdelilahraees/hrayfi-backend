package org.example.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.backend.geo.VillesMaroc;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "artisan")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Artisan extends AppUser {

    @Column(nullable = false, length = 100)
    private String metier;

    @Column(name = "tarif_horaire", precision = 10, scale = 2)
    private BigDecimal tarifHoraire;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String ville;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Disponibilite disponibilite = Disponibilite.DISPONIBLE;

    /** Un nouveau compte attend la validation d'un administrateur avant toute connexion. */
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_validation", nullable = false, length = 20)
    @Builder.Default
    private StatutValidation statutValidation = StatutValidation.EN_ATTENTE;

    /** Raison communiquée à l'artisan en cas de refus ou de suspension */
    @Column(name = "motif_refus", columnDefinition = "TEXT")
    private String motifRefus;

    @Column(name = "valide_le")
    private Instant valideLe;

    /** Position de l'atelier ou de la zone d'intervention (WGS84) */
    private Double latitude;

    private Double longitude;

    /** true : centre de la ville déclarée ; false : position placée par l'artisan sur la carte */
    @Column(name = "position_approx", nullable = false)
    @Builder.Default
    private boolean positionApprox = true;

    /** Échéance de l'offre Pro (aucune restriction à l'échéance : sert au suivi et aux relances) */
    @Column(name = "pro_jusqu_au")
    private Instant proJusquAu;

    /** true : période offerte ; false : au moins un abonnement payé */
    @Column(name = "pro_offert", nullable = false)
    @Builder.Default
    private boolean proOffert = true;

    /** Notes de suivi de l'équipe (appels, relances), jamais montrées à l'artisan */
    @Column(name = "note_admin", columnDefinition = "TEXT")
    private String noteAdmin;

    public StatutPro getStatutPro() {
        if (proJusquAu == null) return StatutPro.AUCUN;
        if (proJusquAu.isBefore(Instant.now())) return StatutPro.EXPIRE;
        return proOffert ? StatutPro.OFFERT : StatutPro.PAYE;
    }

    public boolean isValide() {
        return statutValidation == StatutValidation.VALIDE;
    }

    /**
     * Place l'artisan au centre de sa ville, décalé de quelques kilomètres de façon stable (selon l'e-mail)
     * pour que les artisans d'une même ville ne se superposent pas sur la carte. Sans effet si la ville est inconnue.
     */
    public void positionnerSurVille() {
        VillesMaroc.trouver(ville).ifPresent(v -> {
            int h = Math.abs(String.valueOf(getEmail()).hashCode());
            double angle = (h % 360) * Math.PI / 180;
            double km = 0.8 + (h / 360 % 30) / 10.0; // entre 0,8 et 3,7 km du centre
            latitude = v.latitude() + km / 111.0 * Math.sin(angle);
            longitude = v.longitude() + km / (111.0 * Math.cos(Math.toRadians(v.latitude()))) * Math.cos(angle);
            positionApprox = true;
        });
    }
}
