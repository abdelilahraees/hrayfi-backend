package org.example.backend.paiement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.user.entity.AppUser;
import org.example.backend.user.entity.Artisan;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

/** Paiement encaissé hors ligne par l'équipe et enregistré par un administrateur. */
@Entity
@Table(name = "paiement")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artisan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_paiement_artisan"))
    private Artisan artisan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ObjetPaiement objet;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MoyenPaiement moyen;

    @Column(length = 100)
    private String reference;

    @Column(name = "duree_jours", nullable = false)
    private Integer dureeJours;

    /** Annonce mise en avant (objet MISE_EN_AVANT) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonce_id", foreignKey = @ForeignKey(name = "fk_paiement_annonce"))
    private Annonce annonce;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enregistre_par", foreignKey = @ForeignKey(name = "fk_paiement_admin"))
    private AppUser enregistrePar;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
