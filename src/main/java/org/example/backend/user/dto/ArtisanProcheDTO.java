package org.example.backend.user.dto;

/**
 * Artisan trouvé autour d'un point, avec sa position (arrondie à ~100 m) et sa distance.
 * positionApprox : position au centre de sa ville, l'artisan ne l'a pas encore précisée.
 */
public record ArtisanProcheDTO(
        ArtisanPublicDTO artisan,
        double latitude,
        double longitude,
        boolean positionApprox,
        double distanceKm,
        /** Au moins une annonce mise en avant en cours */
        boolean misEnAvant
) {}
