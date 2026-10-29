package org.example.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.annonce.repository.AnnonceRepository;
import org.example.backend.avis.entity.AvisAnnonce;
import org.example.backend.avis.repository.AvisAnnonceRepository;
import org.example.backend.user.entity.*;
import org.example.backend.user.repository.AppUserRepository;
import org.example.backend.user.repository.ArtisanRepository;
import org.example.backend.user.repository.ClientRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Données de démonstration (SEED_DEMO=true) : quelques artisans avec annonces et avis,
 * plus un client. Ne fait rien si des annonces existent déjà.
 * Comptes : client.demo@hrayfi.ma / artisan.demo@hrayfi.ma — mot de passe : password123
 * Les photos pointent vers les images servies par le front (/img/...).
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements ApplicationRunner {

    private static final String PASSWORD = "password123";

    private final AppUserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ArtisanRepository artisanRepository;
    private final AnnonceRepository annonceRepository;
    private final AvisAnnonceRepository avisRepository;
    private final PasswordEncoder passwordEncoder;

    private record Demo(String email, String prenom, String nom, String metier, String ville, int tarif,
                        String titre, String description, int prixMin, int prixMax, String image, int[] notes) {}

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (annonceRepository.count() > 0 || userRepository.existsByEmail("client.demo@hrayfi.ma")) {
            return;
        }

        Client client = clientRepository.save(Client.builder()
                .email("client.demo@hrayfi.ma").password(passwordEncoder.encode(PASSWORD))
                .nom("Bennani").prenom("Sara").role(Role.CLIENT).adresse("Agdal, Rabat")
                .build());
        Client client2 = clientRepository.save(Client.builder()
                .email("youssef.demo@hrayfi.ma").password(passwordEncoder.encode(PASSWORD))
                .nom("El Idrissi").prenom("Youssef").role(Role.CLIENT).adresse("Maârif, Casablanca")
                .build());

        List<Demo> demos = List.of(
                new Demo("artisan.demo@hrayfi.ma", "Karim", "Alami", "Plomberie", "Casablanca", 150,
                        "Réparation de fuites et débouchage rapide",
                        "Intervention en moins de 2 heures sur Casablanca : fuites sous évier, chasse d'eau, "
                                + "robinetterie, débouchage de canalisations et remplacement de chauffe-eau. Devis gratuit.",
                        150, 600, "/img/service-plomberie.jpg", new int[]{5, 4}),
                new Demo("hamid.demo@hrayfi.ma", "Hamid", "Tazi", "Électricité", "Rabat", 180,
                        "Mise aux normes et dépannage électrique",
                        "Électricien certifié : tableau électrique, prises, éclairage LED, recherche de panne "
                                + "et mise en conformité avant location ou vente.",
                        200, 1500, "/img/service-electricite.jpg", new int[]{5, 5}),
                new Demo("nadia.demo@hrayfi.ma", "Nadia", "Berrada", "Peinture", "Marrakech", 120,
                        "Peinture intérieure et tadelakt",
                        "Peinture de murs et plafonds, enduits décoratifs et tadelakt traditionnel. "
                                + "Protection des sols et nettoyage inclus.",
                        800, 5000, "/img/service-peinture.jpg", new int[]{4}),
                new Demo("omar.demo@hrayfi.ma", "Omar", "Chraibi", "Menuiserie", "Fès", 160,
                        "Menuiserie bois sur mesure",
                        "Portes, placards, cuisines et réparations de meubles en bois massif ou MDF. "
                                + "Prise de mesures à domicile.",
                        500, 8000, "/img/service-menuiserie.jpg", new int[]{3, 4}),
                new Demo("rachid.demo@hrayfi.ma", "Rachid", "Fassi", "Climatisation", "Casablanca", 200,
                        "Installation et entretien de climatiseurs",
                        "Pose de climatiseurs split, recharge de gaz, nettoyage et entretien annuel. "
                                + "Toutes marques.",
                        300, 2500, "/img/service-climatisation.jpg", new int[]{}),
                new Demo("said.demo@hrayfi.ma", "Saïd", "Amrani", "Maçonnerie", "Tanger", 140,
                        "Petits travaux de maçonnerie et carrelage",
                        "Murs, dalles, carrelage, faïence de salle de bain et rénovation de terrasses.",
                        400, 6000, "/img/service-maconnerie.jpg", new int[]{4})
        );

        for (Demo d : demos) {
            Artisan artisan = artisanRepository.save(Artisan.builder()
                    .email(d.email()).password(passwordEncoder.encode(PASSWORD))
                    .nom(d.nom()).prenom(d.prenom()).role(Role.ARTISAN)
                    .metier(d.metier()).ville(d.ville()).tarifHoraire(BigDecimal.valueOf(d.tarif()))
                    .description("Artisan " + d.metier().toLowerCase() + " à " + d.ville() + ".")
                    .disponibilite(Disponibilite.DISPONIBLE)
                    .build());

            Annonce annonce = Annonce.builder()
                    .artisan(artisan).titre(d.titre()).description(d.description())
                    .metier(d.metier()).ville(d.ville())
                    .prixMin(BigDecimal.valueOf(d.prixMin())).prixMax(BigDecimal.valueOf(d.prixMax()))
                    .build();
            annonce.addImage(d.image());

            int[] notes = d.notes();
            if (notes.length > 0) {
                double moyenne = java.util.Arrays.stream(notes).average().orElse(0);
                annonce.setNoteMoyenne(BigDecimal.valueOf(moyenne).setScale(2, RoundingMode.HALF_UP));
                annonce.setNbAvis(notes.length);
            }
            annonceRepository.save(annonce);

            Client[] auteurs = {client, client2};
            for (int i = 0; i < notes.length; i++) {
                avisRepository.save(AvisAnnonce.builder()
                        .annonce(annonce).client(auteurs[i % 2]).note(notes[i])
                        .commentaire(notes[i] >= 4 ? "Travail soigné, ponctuel et prix respecté."
                                                   : "Correct, mais un peu de retard.")
                        .build());
            }
        }
        log.info("Données de démonstration créées ({} artisans)", demos.size());
    }
}
