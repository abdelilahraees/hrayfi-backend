package org.example.backend.common.util;

/**
 * Lecture des lignes (moyenne, nombre) renvoyées par les requêtes d'agrégat JPQL.
 * Spring Data enveloppe parfois la ligne dans un tableau supplémentaire :
 * on la déballe avant de lire les colonnes.
 */
public final class AggregateRows {

    private AggregateRows() {}

    public static double average(Object[] row) {
        Object[] r = unwrap(row);
        return r != null && r.length > 0 && r[0] != null ? ((Number) r[0]).doubleValue() : 0.0;
    }

    public static long count(Object[] row) {
        Object[] r = unwrap(row);
        return r != null && r.length > 1 && r[1] != null ? ((Number) r[1]).longValue() : 0L;
    }

    private static Object[] unwrap(Object[] row) {
        if (row != null && row.length == 1 && row[0] instanceof Object[] inner) return inner;
        return row;
    }
}
