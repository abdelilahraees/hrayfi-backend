package org.example.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

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
}
