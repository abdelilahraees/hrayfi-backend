package org.example.backend.evaluation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.demande.entity.DemandeService;
import org.example.backend.user.entity.Artisan;
import org.example.backend.user.entity.Client;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Note (1-5) + commentaire libre laissé par un client sur une demande TERMINEE.
 * Les FKs client/artisan sont dénormalisées : elles pourraient être déduites de
 * la demande, mais les garder évite un JOIN pour les requêtes d'agrégation
 * dashboard (AVG(note) WHERE artisan_id = ?).
 */
@Entity
@Table(name = "evaluation")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_evaluation_demande"))
    private DemandeService demande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_evaluation_client"))
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artisan_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_evaluation_artisan"))
    private Artisan artisan;

    @Column(nullable = false)
    private Integer note;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}