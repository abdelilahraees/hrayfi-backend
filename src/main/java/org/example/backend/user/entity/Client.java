package org.example.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "client")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Client extends AppUser {

    @Column(length = 255)
    private String adresse;

    @Column(columnDefinition = "TEXT")
    private String preferences;
}
