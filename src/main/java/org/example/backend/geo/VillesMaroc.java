package org.example.backend.geo;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Centre des principales villes du Maroc : position par défaut d'un artisan qui n'a pas placé
 * son atelier sur la carte, et point de départ quand un client refuse la géolocalisation.
 */
public final class VillesMaroc {

    public record Ville(String nom, double latitude, double longitude) {}

    private static final List<Ville> VILLES = List.of(
            new Ville("Casablanca", 33.5731, -7.5898),
            new Ville("Rabat", 34.0209, -6.8416),
            new Ville("Salé", 34.0531, -6.7985),
            new Ville("Marrakech", 31.6295, -7.9811),
            new Ville("Fès", 34.0331, -5.0003),
            new Ville("Tanger", 35.7595, -5.8340),
            new Ville("Agadir", 30.4278, -9.5981),
            new Ville("Meknès", 33.8935, -5.5473),
            new Ville("Oujda", 34.6814, -1.9086),
            new Ville("Kénitra", 34.2610, -6.5802),
            new Ville("Tétouan", 35.5785, -5.3684),
            new Ville("El Jadida", 33.2316, -8.5007),
            new Ville("Mohammedia", 33.6861, -7.3829),
            new Ville("Safi", 32.2994, -9.2372),
            new Ville("Béni Mellal", 32.3373, -6.3498),
            new Ville("Nador", 35.1681, -2.9335),
            new Ville("Essaouira", 31.5085, -9.7595),
            new Ville("Ouarzazate", 30.9335, -6.9370)
    );

    private static final Map<String, Ville> PAR_CLE = new LinkedHashMap<>();
    static {
        VILLES.forEach(v -> PAR_CLE.put(cle(v.nom()), v));
    }

    private VillesMaroc() {}

    public static List<Ville> toutes() {
        return VILLES;
    }

    /** Recherche tolérante : « fes », « Fès » et « FES » désignent la même ville. */
    public static Optional<Ville> trouver(String nom) {
        return nom == null ? Optional.empty() : Optional.ofNullable(PAR_CLE.get(cle(nom)));
    }

    private static String cle(String s) {
        return Normalizer.normalize(s.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
