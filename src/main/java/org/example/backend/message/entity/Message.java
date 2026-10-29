package org.example.backend.message.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.user.entity.AppUser;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "message")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expediteur_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_expediteur"))
    private AppUser expediteur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinataire_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_destinataire"))
    private AppUser destinataire;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(nullable = false)
    private Instant timestamp;

    /** Annonce dont il est question (optionnel) : affichée comme contexte dans le chat. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonce_id", foreignKey = @ForeignKey(name = "fk_message_annonce"))
    private Annonce annonce;

    /** Offre de prix en MAD (optionnel) : transforme le message en proposition de négociation. */
    @Column(name = "prix_propose", precision = 10, scale = 2)
    private BigDecimal prixPropose;

    @Column(nullable = false)
    @Builder.Default
    private boolean lu = false;

    @PrePersist
    void onCreate() {
        if (timestamp == null) timestamp = Instant.now();
    }
}
