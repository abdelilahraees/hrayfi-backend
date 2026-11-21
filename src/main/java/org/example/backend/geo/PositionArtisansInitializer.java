package org.example.backend.geo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.user.entity.Artisan;
import org.example.backend.user.repository.ArtisanRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Au démarrage, place sur la carte les artisans sans position (inscrits avant la V7 ou créés par les seeders)
 * au centre de leur ville. Ne touche jamais une position choisie par l'artisan.
 */
@Component
@Order(10) // après les seeders
@RequiredArgsConstructor
@Slf4j
public class PositionArtisansInitializer implements ApplicationRunner {

    private final ArtisanRepository artisanRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Artisan> sansPosition = artisanRepository.findByLatitudeIsNull();
        sansPosition.forEach(Artisan::positionnerSurVille);
        long places = sansPosition.stream().filter(a -> a.getLatitude() != null).count();
        if (places > 0) log.info("{} artisan(s) placé(s) au centre de leur ville sur la carte", places);
    }
}
