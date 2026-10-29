package org.example.backend.annonce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "annonce_image")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnonceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "annonce_id", nullable = false, foreignKey = @ForeignKey(name = "fk_annonce_image_annonce"))
    private Annonce annonce;

    /** Chemin public servi par /uploads/** (ex : /uploads/annonces/3f2a….jpg) */
    @Column(nullable = false, length = 255)
    private String url;

    @Column(nullable = false)
    private int position;
}
