package org.example.backend.common.util;

import org.springframework.util.StringUtils;

/** Numéros marocains : accepte 06 12 34 56 78, 0612-345678, +212 6…, 00212 6… et normalise en +2126XXXXXXXX. */
public final class Telephone {

    private Telephone() {}

    /** @return le numéro au format +212XXXXXXXXX, ou null si vide */
    public static String normaliser(String brut) {
        if (!StringUtils.hasText(brut)) return null;
        String chiffres = brut.replaceAll("[\\s.\\-()]", "");
        if (chiffres.startsWith("00212")) chiffres = "+212" + chiffres.substring(5);
        else if (chiffres.startsWith("0")) chiffres = "+212" + chiffres.substring(1);
        else if (chiffres.startsWith("212")) chiffres = "+" + chiffres;
        if (!chiffres.matches("\\+212[5-7]\\d{8}")) {
            throw new IllegalArgumentException("Numéro de téléphone marocain invalide (ex. 06 12 34 56 78)");
        }
        return chiffres;
    }
}
