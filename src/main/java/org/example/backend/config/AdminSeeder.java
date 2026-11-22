package org.example.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.annonce.repository.AnnonceRepository;
import org.example.backend.paiement.entity.MoyenPaiement;
import org.example.backend.paiement.entity.ObjetPaiement;
import org.example.backend.paiement.entity.Paiement;
import org.example.backend.paiement.repository.PaiementRepository;
import org.example.backend.user.entity.*;
import org.example.backend.user.repository.AdminRepository;
import org.example.backend.user.repository.AppUserRepository;
import org.example.backend.user.repository.ArtisanRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Compte administrateur de démonstration (admin@hrayfi.ma / password123) et quelques inscriptions
 * d'artisans à traiter, pour essayer la file de validation. Chaque élément n'est créé que s'il manque.
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo", havingValue = "true")
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements ApplicationRunner {

    private static final String PASSWORD = "password123";

    private final AppUserRepository userRepository;
    private final AdminRepository adminRepository;
    private final ArtisanRepository artisanRepository;
    private final PasswordEncoder passwordEncoder;
    private final AnnonceRepository annonceRepository;
    private final PaiementRepository paiementRepository;
    private final JdbcTemplate jdbc;

    private record Inscription(String email, String prenom, String nom, String metier, String ville, int tarif,
                               String description, StatutValidation statut, String motif) {}

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByEmail("admin@hrayfi.ma")) {
            adminRepository.save(Admin.builder()
                    .email("admin@hrayfi.ma").password(passwordEncoder.encode(PASSWORD))
                    .prenom("Équipe").nom("Hrayfi").role(Role.ADMIN)
                    .build());
            log.info("Compte administrateur de démonstration créé : admin@hrayfi.ma");
        }

        List<Inscription> inscriptions = List.of(
                new Inscription("attente.plombier@hrayfi.ma", "Adil", "Benmoussa", "Plomberie", "Casablanca", 140,
                        "Plombier depuis 8 ans, spécialisé en chauffe-eau et salles de bain.", StatutValidation.EN_ATTENTE, null),
                new Inscription("attente.zellige@hrayfi.ma", "Mohamed", "Lazrak", "Maçonnerie", "Fès", 170,
                        "Maâlem zelligeur, fontaines et sols traditionnels, 20 ans de métier.", StatutValidation.EN_ATTENTE, null),
                new Inscription("attente.electricien@hrayfi.ma", "Yassine", "Ouali", "Électricité", "Rabat", 160,
                        "Électricien bâtiment, installations neuves et dépannage.", StatutValidation.EN_ATTENTE, null),
                new Inscription("attente.peintre@hrayfi.ma", "Samira", "Haddad", "Peinture", "Agadir", 110,
                        "Peinture intérieure et décoration, travail soigné.", StatutValidation.EN_ATTENTE, null),
                new Inscription("refuse.demo@hrayfi.ma", "Test", "Doublon", "Menuiserie", "Tanger", 100,
                        "Compte en double.", StatutValidation.REFUSE, "Compte en double : un profil existe déjà avec ces informations.")
        );
        int crees = 0;
        for (Inscription i : inscriptions) {
            if (userRepository.existsByEmail(i.email())) continue;
            artisanRepository.save(Artisan.builder()
                    .email(i.email()).password(passwordEncoder.encode(PASSWORD))
                    .prenom(i.prenom()).nom(i.nom()).role(Role.ARTISAN)
                    .metier(i.metier()).ville(i.ville()).tarifHoraire(BigDecimal.valueOf(i.tarif()))
                    .description(i.description())
                    .statutValidation(i.statut()).motifRefus(i.motif())
                    .build());
            crees++;
        }
        if (crees > 0) log.info("{} inscription(s) d'artisans à valider créées", crees);

        telephonesDemo();
        paiementsDemo();
    }

    /** Numéros fictifs (06 00 xx xx xx) pour les comptes de démonstration qui n'en ont pas */
    private void telephonesDemo() {
        int n = 0;
        for (AppUser u : userRepository.findAll()) {
            if (u.getTelephone() == null && u.getEmail().endsWith("@hrayfi.ma") && u.getRole() != Role.ADMIN) {
                u.setTelephone(String.format("+2126%08d", u.getId() * 7919 % 100_000_000));
                n++;
            }
        }
        if (n > 0) log.info("{} numéro(s) de téléphone de démonstration ajoutés", n);
    }

    /**
     * Quelques abonnements payés et mises en avant sur 6 mois, et des échéances variées (expirées, proches),
     * pour remplir le suivi des comptes et les revenus. Une seule fois : ne fait rien si un paiement existe.
     */
    private void paiementsDemo() {
        if (paiementRepository.count() > 0) return;
        AppUser admin = userRepository.findByEmail("admin@hrayfi.ma").orElse(null);
        List<Artisan> artisans = artisanRepository.findAll().stream()
                .filter(Artisan::isValide).sorted(Comparator.comparing(Artisan::getId)).toList();
        if (artisans.size() < 12) return;
        Random rnd = new Random(7);
        Instant maintenant = Instant.now();
        MoyenPaiement[] moyens = {MoyenPaiement.CASHPLUS, MoyenPaiement.CASHPLUS, MoyenPaiement.WAFACASH, MoyenPaiement.VIREMENT, MoyenPaiement.ESPECES};
        int n = 0;
        for (int i = 0; i < artisans.size(); i++) {
            Artisan a = artisans.get(i);
            int profil = i % 6;
            if (profil == 0 || profil == 1) {
                // Abonnés payants : 1 à 3 paiements mensuels ou trimestriels
                int nb = 1 + rnd.nextInt(3);
                for (int k = 0; k < nb; k++) {
                    boolean trimestre = rnd.nextBoolean();
                    Paiement p = paiementRepository.save(Paiement.builder().artisan(a).objet(ObjetPaiement.ABONNEMENT_PRO)
                            .montant(BigDecimal.valueOf(trimestre ? 399 : 149)).moyen(moyens[rnd.nextInt(moyens.length)])
                            .reference("CP" + (100000 + rnd.nextInt(900000))).dureeJours(trimestre ? 90 : 30).enregistrePar(admin).build());
                    antidater(p, 5 + rnd.nextInt(170));
                    n++;
                }
                a.setProOffert(false);
                a.setProJusquAu(maintenant.plus(10 + rnd.nextInt(80), ChronoUnit.DAYS));
            } else if (profil == 2) {
                a.setProJusquAu(maintenant.minus(1 + rnd.nextInt(20), ChronoUnit.DAYS)); // offre expirée : à relancer
            } else if (profil == 3) {
                a.setProJusquAu(maintenant.plus(2 + rnd.nextInt(10), ChronoUnit.DAYS)); // échéance proche
            }
            if (i % 7 == 0) {
                annonceRepository.findByArtisanIdOrderByCreatedAtDesc(a.getId()).stream().filter(Annonce::isActive).findFirst().ifPresent(an -> {
                    an.setSponsoriseJusquAu(maintenant.plus(3 + rnd.nextInt(12), ChronoUnit.DAYS));
                    Paiement p = paiementRepository.save(Paiement.builder().artisan(a).objet(ObjetPaiement.MISE_EN_AVANT)
                            .montant(BigDecimal.valueOf(49)).moyen(MoyenPaiement.CASHPLUS).dureeJours(14).annonce(an).enregistrePar(admin).build());
                    antidater(p, rnd.nextInt(10));
                });
                n++;
            }
        }
        log.info("{} paiement(s) de démonstration créés", n);
    }

    /** @CreatedDate impose la date du jour à l'insertion : on la recule ensuite pour étaler les revenus */
    private void antidater(Paiement p, int jours) {
        paiementRepository.flush();
        jdbc.update("UPDATE paiement SET created_at = ? WHERE id = ?",
                Timestamp.from(Instant.now().minus(jours, ChronoUnit.DAYS)), p.getId());
    }
}
