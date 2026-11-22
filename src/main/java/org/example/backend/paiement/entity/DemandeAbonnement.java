package org.example.backend.paiement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.user.entity.AppUser;
import org.example.backend.user.entity.Artisan;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/** Demande de changement d'offre envoyée par un artisan depuis son espace, traitée par l'équipe. */
@Entity
@Table(name = "demande_abonnement")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeAbonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artisan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dab_artisan"))
    private Artisan artisan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Formule formule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonce_id", foreignKey = @ForeignKey(name = "fk_dab_annonce"))
    private Annonce annonce;

    @Enumerated(EnumType.STRING)
    @Column(name = "moyen_souhaite", nullable = false, length = 20)
    private MoyenPaiement moyenSouhaite;

    /** Message libre de l'artisan à l'équipe */
    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutDemandeAbonnement statut = StatutDemandeAbonnement.EN_ATTENTE;

    /** Réponse de l'équipe, montrée à l'artisan */
    @Column(name = "reponse_admin", columnDefinition = "TEXT")
    private String reponseAdmin;

    /** Paiement enregistré quand la demande est payée */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paiement_id", foreignKey = @ForeignKey(name = "fk_dab_paiement"))
    private Paiement paiement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traitee_par", foreignKey = @ForeignKey(name = "fk_dab_admin"))
    private AppUser traiteePar;

    @Column(name = "traitee_le")
    private Instant traiteeLe;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public boolean isOuverte() {
        return statut == StatutDemandeAbonnement.EN_ATTENTE || statut == StatutDemandeAbonnement.CONTACTEE;
    }
}
