package org.example.backend.user.repository;

import org.example.backend.user.entity.Artisan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtisanRepository extends JpaRepository<Artisan, Long> {
    List<Artisan> findByMetierIgnoreCaseAndVilleIgnoreCase(String metier, String ville);
    List<Artisan> findByMetierIgnoreCase(String metier);
}
