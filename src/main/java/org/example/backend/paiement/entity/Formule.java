package org.example.backend.paiement.entity;

import java.math.BigDecimal;
import java.util.List;

/**
 * Catalogue des offres payantes, source unique des prix, durées et avantages affichés
 * aux artisans (demande d'offre) et aux administrateurs (enregistrement d'un paiement).
 * Aucune fonctionnalité n'est bloquée sans offre : le Pro apporte de la visibilité et un accompagnement.
 */
public enum Formule {
    PRO_MOIS(ObjetPaiement.ABONNEMENT_PRO, "Offre Pro · 1 mois", 149, 30, AvantagesPro.LISTE),
    PRO_TRIMESTRE(ObjetPaiement.ABONNEMENT_PRO, "Offre Pro · 3 mois", 399, 90, AvantagesPro.LISTE),
    PRO_AN(ObjetPaiement.ABONNEMENT_PRO, "Offre Pro · 1 an", 1490, 365, AvantagesPro.LISTE),
    MISE_EN_AVANT_14(ObjetPaiement.MISE_EN_AVANT, "Mise en avant · 14 jours", 49, 14, AvantagesMiseEnAvant.LISTE),
    MISE_EN_AVANT_30(ObjetPaiement.MISE_EN_AVANT, "Mise en avant · 30 jours", 89, 30, AvantagesMiseEnAvant.LISTE);

    private final ObjetPaiement objet;
    private final String libelle;
    private final BigDecimal montant;
    private final int dureeJours;
    private final List<String> avantages;

    Formule(ObjetPaiement objet, String libelle, int montant, int dureeJours, List<String> avantages) {
        this.objet = objet;
        this.libelle = libelle;
        this.montant = BigDecimal.valueOf(montant);
        this.dureeJours = dureeJours;
        this.avantages = avantages;
    }

    public ObjetPaiement objet() { return objet; }
    public String libelle() { return libelle; }
    public BigDecimal montant() { return montant; }
    public int dureeJours() { return dureeJours; }
    public List<String> avantages() { return avantages; }

    private static final class AvantagesPro {
        static final List<String> LISTE = List.of(
                "Badge « Artisan Pro » sur votre profil, vos annonces et la carte",
                "Tableau de bord complet : agenda, revenus, qualité du profil",
                "Contact WhatsApp direct par les clients connectés",
                "Accompagnement de l'équipe Hrayfi pour améliorer vos annonces");
    }

    private static final class AvantagesMiseEnAvant {
        static final List<String> LISTE = List.of(
                "Votre annonce en tête des résultats de recherche",
                "Badge « Mis en avant » sur l'annonce et sur la carte",
                "Plus de visites : les clients regardent d'abord ces emplacements");
    }
}
