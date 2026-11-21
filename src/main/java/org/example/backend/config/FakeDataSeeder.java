package org.example.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.annonce.repository.AnnonceRepository;
import org.example.backend.avis.entity.AvisAnnonce;
import org.example.backend.avis.repository.AvisAnnonceRepository;
import org.example.backend.demande.entity.DemandeService;
import org.example.backend.demande.entity.StatutDemande;
import org.example.backend.demande.repository.DemandeServiceRepository;
import org.example.backend.evaluation.entity.Evaluation;
import org.example.backend.evaluation.repository.EvaluationRepository;
import org.example.backend.message.entity.Message;
import org.example.backend.message.repository.MessageRepository;
import org.example.backend.user.entity.*;
import org.example.backend.user.repository.AppUserRepository;
import org.example.backend.user.repository.ArtisanRepository;
import org.example.backend.user.repository.ClientRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Données factices en volume pour tester la pagination, les filtres et les tableaux de bord (SEED_FAKE=true).
 * Crée ~36 artisans, ~70 annonces, plusieurs centaines d'avis, des demandes sur 6 mois et des conversations,
 * y compris pour les comptes de démonstration. Ne s'exécute qu'une fois (repère : fake.artisan01@hrayfi.ma).
 * Tirage aléatoire à graine fixe : les mêmes données à chaque base neuve. Mot de passe : password123
 */
@Component
@ConditionalOnProperty(name = "app.seed-fake", havingValue = "true")
@Order(2) // après DemoDataSeeder, pour rattacher des demandes aux comptes de démonstration
@RequiredArgsConstructor
@Slf4j
public class FakeDataSeeder implements ApplicationRunner {

    private static final String PASSWORD = "password123";
    private static final String MARKER = "fake.artisan01@hrayfi.ma";

    private final AppUserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ArtisanRepository artisanRepository;
    private final AnnonceRepository annonceRepository;
    private final AvisAnnonceRepository avisRepository;
    private final DemandeServiceRepository demandeRepository;
    private final EvaluationRepository evaluationRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random rnd = new Random(42);

    private static final String[] PRENOMS = {"Youssef", "Fatima Zahra", "Mehdi", "Khadija", "Hamza", "Salma", "Anas", "Imane",
            "Othmane", "Meryem", "Ayoub", "Hajar", "Reda", "Sanae", "Amine", "Loubna", "Ilyas", "Nisrine", "Soufiane", "Kawtar",
            "Abdelkader", "Houda", "Brahim", "Zineb", "Mustapha", "Asmae", "Driss", "Ghita", "Hicham", "Rajae"};
    private static final String[] NOMS = {"El Amrani", "Bennani", "Alaoui", "Tazi", "Chraibi", "Idrissi", "Berrada", "Fassi",
            "Lahlou", "Benjelloun", "Ouazzani", "Kettani", "Sqalli", "Bouzidi", "El Khatib", "Naciri", "Ziani", "Tahiri",
            "Benkirane", "Mansouri", "Lamrani", "Sebti", "Guessous", "Hajji"};
    private static final String[] VILLES = {"Casablanca", "Rabat", "Marrakech", "Fès", "Tanger", "Agadir", "Meknès",
            "Oujda", "Kénitra", "Tétouan", "Salé", "El Jadida"};

    private record Metier(String nom, String image, int prixMin, int prixMax, int tarif, String[] titres, String[] descriptions) {}

    private static final List<Metier> METIERS = List.of(
            new Metier("Plomberie", "/img/service-plomberie.jpg", 150, 1200, 150,
                    new String[]{"Dépannage plomberie 7j/7", "Remplacement de chauffe-eau", "Débouchage de canalisations",
                            "Rénovation de salle de bain", "Recherche et réparation de fuites"},
                    new String[]{"Intervention rapide pour fuites, robinetterie et chasse d'eau. Devis gratuit avant travaux.",
                            "Pose et remplacement de chauffe-eau électrique ou à gaz, toutes marques, garantie un an.",
                            "Débouchage d'évier, de douche et de WC à l'aide d'un furet professionnel, sans casse."}),
            new Metier("Électricité", "/img/service-electricite.jpg", 200, 2500, 180,
                    new String[]{"Mise aux normes électriques", "Installation de tableau électrique", "Dépannage électrique urgent",
                            "Éclairage LED intérieur et extérieur", "Pose de prises et interrupteurs"},
                    new String[]{"Électricien qualifié : recherche de panne, remplacement de disjoncteurs et mise en conformité.",
                            "Installation complète pour appartement neuf ou rénovation, schéma et attestation fournis.",
                            "Éclairage économique LED, spots encastrés et éclairage de jardin."}),
            new Metier("Peinture", "/img/service-peinture.jpg", 600, 8000, 120,
                    new String[]{"Peinture intérieure soignée", "Tadelakt traditionnel", "Ravalement de façade",
                            "Enduits décoratifs et stucs", "Peinture de portes et boiseries"},
                    new String[]{"Préparation des murs, deux couches de finition, protection des sols et nettoyage inclus.",
                            "Tadelakt à la chaux de Marrakech pour salles de bain, hammams et murs décoratifs.",
                            "Rénovation de façades : nettoyage, traitement des fissures et peinture extérieure."}),
            new Metier("Menuiserie", "/img/service-menuiserie.jpg", 500, 15000, 160,
                    new String[]{"Cuisine sur mesure en bois", "Placards et dressings", "Portes en bois de cèdre",
                            "Réparation de meubles anciens", "Moucharabieh et bois sculpté"},
                    new String[]{"Conception et fabrication sur mesure, prise de mesures à domicile et pose incluse.",
                            "Travail du cèdre et du noyer, finitions vernies ou huilées selon vos goûts.",
                            "Restauration de meubles traditionnels et pièces sculptées à la main."}),
            new Metier("Maçonnerie", "/img/service-maconnerie.jpg", 400, 20000, 140,
                    new String[]{"Pose de zellige artisanal", "Carrelage et faïence", "Rénovation de terrasse",
                            "Construction de murs et cloisons", "Petits travaux de maçonnerie"},
                    new String[]{"Pose de zellige de Fès pour fontaines, sols et murs, découpe à la main.",
                            "Carrelage de sols et faïence de salle de bain, joints soignés et nettoyage de fin de chantier.",
                            "Étanchéité et carrelage de terrasses, réparation de fissures et dalles."}),
            new Metier("Climatisation", "/img/service-climatisation.jpg", 300, 6000, 200,
                    new String[]{"Installation de climatiseur split", "Entretien annuel de climatisation", "Recharge de gaz frigorigène",
                            "Dépannage climatisation", "Pompe à chaleur réversible"},
                    new String[]{"Pose de climatiseurs muraux toutes marques, raccordement et mise en service.",
                            "Nettoyage des filtres, contrôle de pression et désinfection, idéal avant l'été.",
                            "Diagnostic de panne, recherche de fuite et recharge de gaz."})
    );

    private static final String[] PHOTOS_EXTRA = {"/img/step-1.jpg", "/img/step-2.jpg", "/img/step-3.jpg", "/img/step-4.jpg", "/img/hero-main.jpg"};

    private static final String[][] COMMENTAIRES = {
            {}, // index 0 inutilisé
            {"Très déçu : l'artisan est arrivé avec trois heures de retard et le travail a dû être refait.",
                    "Ne répond plus au téléphone depuis la fin du chantier. Je ne recommande pas."},
            {"Travail correct mais finitions bâclées, il a fallu le rappeler deux fois.",
                    "Prix plus élevé que le devis annoncé au départ.", "Résultat moyen, peu de communication."},
            {"Correct dans l'ensemble, un peu de retard sur le planning.", "Bon travail, mais le chantier n'a pas été nettoyé.",
                    "Prix raisonnable, résultat conforme à ce qui était prévu."},
            {"Travail soigné, ponctuel et prix respecté.", "Très professionnel, je referai appel à lui.",
                    "Bon artisan, de bons conseils et un résultat propre.", "Intervention rapide, explications claires. Merci !",
                    "Bon rapport qualité-prix, chantier laissé propre."},
            {"Excellent ! Travail impeccable et très bon contact. Allah y3tik saha.", "Un vrai maâlem, du travail à l'ancienne. Je recommande vivement.",
                    "Parfait du début à la fin : devis clair, délai respecté, résultat superbe.",
                    "Très satisfaite, l'artisan est venu le jour même. Baraka Allahou fik.",
                    "Rien à redire, travail d'une grande qualité et très bon accueil.", "Le meilleur artisan que nous ayons eu à la maison."}
    };

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(MARKER)) {
            return;
        }

        // ---- Clients ----
        List<Client> clients = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            String prenom = pick(PRENOMS);
            clients.add(clientRepository.save(Client.builder()
                    .email(String.format("fake.client%02d@hrayfi.ma", i)).password(passwordEncoder.encode(PASSWORD))
                    .prenom(prenom).nom(pick(NOMS)).role(Role.CLIENT)
                    .adresse(pick(VILLES))
                    .build()));
        }
        Client sara = clientOrNull("client.demo@hrayfi.ma");
        Client youssef = clientOrNull("youssef.demo@hrayfi.ma");
        List<Client> auteurs = new ArrayList<>(clients);
        if (sara != null) auteurs.add(sara);
        if (youssef != null) auteurs.add(youssef);

        // ---- Artisans, annonces, avis ----
        List<Artisan> artisans = new ArrayList<>();
        List<Annonce> annonces = new ArrayList<>();
        int nbAvis = 0;
        for (int i = 1; i <= 36; i++) {
            Metier m = METIERS.get((i - 1) % METIERS.size());
            String ville = pick(VILLES);
            Artisan artisan = artisanRepository.save(Artisan.builder()
                    .email(String.format("fake.artisan%02d@hrayfi.ma", i)).password(passwordEncoder.encode(PASSWORD))
                    .prenom(pick(PRENOMS)).nom(pick(NOMS)).role(Role.ARTISAN)
                    .metier(m.nom()).ville(ville).tarifHoraire(BigDecimal.valueOf(m.tarif() + 10L * rnd.nextInt(6)))
                    .description((5 + rnd.nextInt(20)) + " ans d'expérience en " + m.nom().toLowerCase() + " à " + ville
                            + ". Travail soigné, devis gratuit et respect des délais.")
                    .disponibilite(Disponibilite.values()[rnd.nextInt(10) < 7 ? 0 : 1 + rnd.nextInt(Disponibilite.values().length - 1)])
                    .statutValidation(StatutValidation.VALIDE).valideLe(Instant.now())
                    .build());
            artisans.add(artisan);

            int nbAnnonces = 1 + rnd.nextInt(3);
            for (int k = 0; k < nbAnnonces; k++) {
                int min = m.prixMin() + 50 * rnd.nextInt(5);
                Annonce annonce = Annonce.builder()
                        .artisan(artisan).titre(m.titres()[rnd.nextInt(m.titres().length)])
                        .description(pick(m.descriptions()) + " Intervention à " + ville + " et environs.")
                        .metier(m.nom()).ville(ville)
                        .prixMin(BigDecimal.valueOf(min)).prixMax(BigDecimal.valueOf(min + m.prixMax() / 2 + 100L * rnd.nextInt(10)))
                        .build();
                annonce.setActive(rnd.nextInt(10) > 0);
                annonce.addImage(m.image());
                for (int p = rnd.nextInt(4); p > 0; p--) annonce.addImage(pick(PHOTOS_EXTRA));

                // Avis de clients distincts (contrainte unique annonce + client)
                Collections.shuffle(auteurs, rnd);
                int n = rnd.nextInt(10) < 2 ? 0 : 1 + rnd.nextInt(12);
                int qualite = rnd.nextInt(3); // certains artisans sont mieux notés que d'autres
                List<AvisAnnonce> avis = new ArrayList<>();
                for (int a = 0; a < Math.min(n, auteurs.size()); a++) {
                    int note = note(qualite);
                    avis.add(AvisAnnonce.builder().annonce(annonce).client(auteurs.get(a)).note(note)
                            .commentaire(rnd.nextInt(8) == 0 ? null : pick(COMMENTAIRES[note])).build());
                }
                if (!avis.isEmpty()) {
                    double moyenne = avis.stream().mapToInt(AvisAnnonce::getNote).average().orElse(0);
                    annonce.setNoteMoyenne(BigDecimal.valueOf(moyenne).setScale(2, RoundingMode.HALF_UP));
                    annonce.setNbAvis(avis.size());
                }
                annonces.add(annonceRepository.save(annonce));
                avisRepository.saveAll(avis);
                nbAvis += avis.size();
            }
        }

        // ---- Demandes, évaluations et messages ----
        int nbDemandes = 0;
        Artisan karim = userRepository.findByEmail("artisan.demo@hrayfi.ma")
                .filter(Artisan.class::isInstance).map(Artisan.class::cast).orElse(null);
        if (karim != null) {
            Annonce annonceKarim = annonceRepository.findAll().stream()
                    .filter(a -> a.getArtisan().getId().equals(karim.getId())).findFirst().orElse(null);
            for (int i = 0; i < 34; i++) {
                nbDemandes += demande(pick(clients), karim, annonceKarim);
            }
            for (int i = 0; i < 6; i++) conversation(pick(clients), karim, annonceKarim);
        }
        if (sara != null) {
            for (int i = 0; i < 18; i++) {
                Annonce a = pickAnnonce(annonces);
                nbDemandes += demande(sara, a.getArtisan(), a);
            }
            for (int i = 0; i < 5; i++) {
                Annonce a = pickAnnonce(annonces);
                conversation(sara, a.getArtisan(), a);
            }
        }
        // Un peu d'activité entre comptes factices, pour les statistiques des artisans
        for (int i = 0; i < 80; i++) {
            Annonce a = pickAnnonce(annonces);
            nbDemandes += demande(pick(clients), a.getArtisan(), a);
        }

        log.info("Données factices créées : {} clients, {} artisans, {} annonces, {} avis, {} demandes",
                clients.size(), artisans.size(), annonces.size(), nbAvis, nbDemandes);
    }

    /**
     * Crée une demande à un statut varié : passées et terminées (avec montant) sur 6 mois,
     * acceptées dans les 7 prochains jours, en attente ou refusées. Retourne 1.
     */
    private int demande(Client client, Artisan artisan, Annonce annonce) {
        int tirage = rnd.nextInt(100);
        StatutDemande statut;
        Instant date;
        if (tirage < 45) {
            statut = StatutDemande.TERMINEE;
            date = Instant.now().minus(1 + rnd.nextInt(175), ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);
        } else if (tirage < 65) {
            statut = StatutDemande.ACCEPTEE;
            date = Instant.now().plus(rnd.nextInt(7 * 24), ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
        } else if (tirage < 88) {
            statut = StatutDemande.EN_ATTENTE;
            date = Instant.now().plus(1 + rnd.nextInt(20), ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);
        } else {
            statut = StatutDemande.REFUSEE;
            date = Instant.now().plus(rnd.nextInt(15) - 7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);
        }
        BigDecimal convenu = annonce == null ? null
                : annonce.getPrixMin().add(BigDecimal.valueOf(50L * rnd.nextInt(10))).setScale(2, RoundingMode.HALF_UP);
        DemandeService d = demandeRepository.save(DemandeService.builder()
                .client(client).artisan(artisan).annonce(annonce)
                .description(pick(new String[]{
                        "Fuite sous l'évier de la cuisine, besoin d'une intervention rapide.",
                        "Rénovation complète de la salle de bain, devis souhaité.",
                        "Plusieurs prises ne fonctionnent plus dans le salon.",
                        "Repeindre deux chambres et le couloir avant l'Aïd.",
                        "Installer un climatiseur dans la chambre parentale.",
                        "Refaire le zellige de la fontaine du patio.",
                        "Placard sur mesure pour l'entrée, environ 2 m de large.",
                        "Le chauffe-eau ne chauffe plus depuis ce matin."}))
                .dateIntervention(date).statut(statut).prixConvenu(rnd.nextBoolean() ? convenu : null)
                .prixFinal(statut == StatutDemande.TERMINEE && convenu != null ? convenu.add(BigDecimal.valueOf(25L * rnd.nextInt(8))) : null)
                .build());
        if (statut == StatutDemande.TERMINEE && d.getPrixFinal() == null) {
            d.setPrixFinal(BigDecimal.valueOf(200 + 50L * rnd.nextInt(30)));
        }
        if (statut == StatutDemande.TERMINEE && rnd.nextInt(10) < 6) {
            int note = note(rnd.nextInt(3));
            evaluationRepository.save(Evaluation.builder().demande(d).client(client).artisan(artisan)
                    .note(note).commentaire(pick(COMMENTAIRES[note])).build());
        }
        return 1;
    }

    /** Petit échange client / artisan, éventuellement avec une offre de prix. */
    private void conversation(Client client, Artisan artisan, Annonce annonce) {
        Instant t = Instant.now().minus(1 + rnd.nextInt(20), ChronoUnit.DAYS);
        String[][] echanges = {
                {"Salam, vous êtes disponible cette semaine ?", "Wa alaykoum salam, oui à partir de mercredi."},
                {"Bonjour, combien pour l'intervention décrite dans l'annonce ?", "Bonjour, il faut que je voie sur place, je peux passer demain."},
                {"Est-ce que vous fournissez le matériel ?", "Oui, le matériel est compris dans le devis."},
                {"Merci pour le travail d'hier, tout fonctionne.", "Avec plaisir, n'hésitez pas si besoin."},
        };
        String[] e = echanges[rnd.nextInt(echanges.length)];
        messageRepository.save(Message.builder().expediteur(client).destinataire(artisan).annonce(annonce)
                .contenu(e[0]).timestamp(t).lu(true).build());
        messageRepository.save(Message.builder().expediteur(artisan).destinataire(client).annonce(annonce)
                .contenu(e[1]).timestamp(t.plus(40 + rnd.nextInt(200), ChronoUnit.MINUTES)).lu(rnd.nextBoolean()).build());
        if (annonce != null && rnd.nextBoolean()) {
            messageRepository.save(Message.builder().expediteur(client).destinataire(artisan).annonce(annonce)
                    .contenu("Je vous propose ce prix, ça vous convient ?")
                    .prixPropose(annonce.getPrixMin().add(BigDecimal.valueOf(50)))
                    .timestamp(t.plus(5, ChronoUnit.HOURS)).lu(false).build());
        }
    }

    /** Note de 1 à 5, tirée selon la qualité de l'artisan (0 moyen, 1 bon, 2 excellent). */
    private int note(int qualite) {
        int r = rnd.nextInt(100);
        return switch (qualite) {
            case 2 -> r < 70 ? 5 : r < 95 ? 4 : 3;
            case 1 -> r < 35 ? 5 : r < 80 ? 4 : r < 95 ? 3 : 2;
            default -> r < 15 ? 5 : r < 45 ? 4 : r < 75 ? 3 : r < 90 ? 2 : 1;
        };
    }

    private Annonce pickAnnonce(List<Annonce> annonces) {
        return annonces.get(rnd.nextInt(annonces.size()));
    }

    private Client clientOrNull(String email) {
        return userRepository.findByEmail(email).filter(Client.class::isInstance).map(Client.class::cast).orElse(null);
    }

    private <T> T pick(T[] values) {
        return values[rnd.nextInt(values.length)];
    }

    private <T> T pick(List<T> values) {
        return values.get(rnd.nextInt(values.size()));
    }
}
