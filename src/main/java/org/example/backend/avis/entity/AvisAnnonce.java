package org.example.backend.avis.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.user.entity.Client;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "avis_annonce")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvisAnnonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "annonce_id", nullable = false, foreignKey = @ForeignKey(name = "fk_avis_annonce"))
    private Annonce annonce;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_avis_client"))
    private Client client;

    @Column(nullable = false)
    private Integer note;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
