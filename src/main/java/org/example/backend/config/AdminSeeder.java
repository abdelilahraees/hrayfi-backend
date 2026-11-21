package org.example.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.user.entity.*;
import org.example.backend.user.repository.AdminRepository;
import org.example.backend.user.repository.AppUserRepository;
import org.example.backend.user.repository.ArtisanRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
    }
}
