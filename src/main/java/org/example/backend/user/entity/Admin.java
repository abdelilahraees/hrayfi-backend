package org.example.backend.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** Administrateur de la plateforme : valide les artisans et modère les contenus. */
@Entity
@Table(name = "admin")
@PrimaryKeyJoinColumn(name = "id")
@SuperBuilder
@NoArgsConstructor
public class Admin extends AppUser {
}
