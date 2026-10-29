package org.example.backend.annonce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.user.entity.Artisan;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Annonce de service publiée par un artisan (titre, description, photos, fourchette de prix).
 * noteMoyenne / nbAvis sont recalculés à chaque avis : la recherche peut filtrer
 * et trier par note sans agrégat.
 */
@Entity
@Table(name = "annonce")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Annonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artisan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_annonce_artisan"))
    private Artisan artisan;

    @Column(nullable = false, length = 150)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String metier;

    @Column(length = 100)
    private String ville;

    @Column(name = "prix_min", precision = 10, scale = 2)
    private BigDecimal prixMin;

    @Column(name = "prix_max", precision = 10, scale = 2)
    private BigDecimal prixMax;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "note_moyenne", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal noteMoyenne = BigDecimal.ZERO;

    @Column(name = "nb_avis", nullable = false)
    @Builder.Default
    private int nbAvis = 0;

    @OneToMany(mappedBy = "annonce", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private List<AnnonceImage> images = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addImage(String url) {
        images.add(AnnonceImage.builder()
                .annonce(this)
                .url(url)
                .position(images.size())
                .build());
    }
}
