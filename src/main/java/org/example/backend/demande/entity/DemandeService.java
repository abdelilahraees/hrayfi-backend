package org.example.backend.demande.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.user.entity.Artisan;
import org.example.backend.user.entity.Client;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "demande_service")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LAZY : évite de charger le client à chaque GET sur la demande (perf + anti-N+1)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_demande_client"))
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artisan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_demande_artisan"))
    private Artisan artisan;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_intervention", nullable = false)
    private Instant dateIntervention;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutDemande statut;

    @Column(name = "prix_final", precision = 10, scale = 2)
    private BigDecimal prixFinal;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
