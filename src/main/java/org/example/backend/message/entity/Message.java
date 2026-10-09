package org.example.backend.message.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.user.entity.AppUser;

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

    @PrePersist
    void onCreate() {
        if (timestamp == null) timestamp = Instant.now();
    }
}
