package org.example.backend.admin.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.admin.dto.*;
import org.example.backend.annonce.dto.AnnonceDTO;
import org.example.backend.annonce.mapper.AnnonceMapper;
import org.example.backend.annonce.repository.AnnonceRepository;
import org.example.backend.annonce.service.AnnonceService;
import org.example.backend.avis.dto.AvisDTO;
import org.example.backend.avis.mapper.AvisMapper;
import org.example.backend.avis.repository.AvisAnnonceRepository;
import org.example.backend.avis.service.AvisService;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.demande.dto.DemandeServiceDTO;
import org.example.backend.demande.entity.StatutDemande;
import org.example.backend.demande.mapper.DemandeServiceMapper;
import org.example.backend.demande.repository.DemandeServiceRepository;
import org.example.backend.notification.entity.NotificationType;
import org.example.backend.notification.service.NotificationService;
import org.example.backend.user.entity.*;
import org.example.backend.user.repository.AppUserRepository;
import org.example.backend.user.repository.ArtisanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Administration de la plateforme : validation des artisans, consultation des comptes,
 * modération des annonces et des avis, suivi des demandes.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AppUserRepository userRepository;
    private final ArtisanRepository artisanRepository;
    private final AnnonceRepository annonceRepository;
    private final AvisAnnonceRepository avisRepository;
    private final DemandeServiceRepository demandeRepository;
    private final AnnonceService annonceService;
    private final AvisService avisService;
    private final NotificationService notificationService;
    private final AnnonceMapper annonceMapper;
    private final AvisMapper avisMapper;
    private final DemandeServiceMapper demandeMapper;

    @Transactional(readOnly = true)
    public AdminStatsDTO stats() {
        Map<String, Long> artisansParStatut = new LinkedHashMap<>();
        Arrays.stream(StatutValidation.values())
                .forEach(s -> artisansParStatut.put(s.name(), artisanRepository.countByStatutValidation(s)));
        Map<String, Long> demandesParStatut = new LinkedHashMap<>();
        Arrays.stream(StatutDemande.values())
                .forEach(s -> demandesParStatut.put(s.name(), demandeRepository.countByStatut(s)));
        Double moyenne = avisRepository.noteMoyenneGlobale();
        return new AdminStatsDTO(
                userRepository.countByRole(Role.CLIENT),
                userRepository.countByRole(Role.ARTISAN),
                artisansParStatut,
                annonceRepository.count(),
                annonceRepository.countByActiveTrue(),
                demandeRepository.count(),
                demandesParStatut,
                avisRepository.count(),
                moyenne == null ? 0 : Math.round(moyenne * 100.0) / 100.0,
                demandeRepository.volumeAffaires()
        );
    }

    // ---------- artisans ----------

    @Transactional(readOnly = true)
    public Page<AdminArtisanDTO> artisans(StatutValidation statut, String q, Pageable pageable) {
        return artisanRepository.adminSearch(statut, like(q), pageable).map(this::toAdminArtisan);
    }

    /**
     * Change le statut d'un artisan. Un refus ou une suspension exige un motif, montré à l'artisan
     * lorsqu'il tente de se connecter ; une validation lui envoie une notification.
     */
    @Transactional
    public AdminArtisanDTO changerStatut(Long artisanId, ChangerStatutRequest req) {
        Artisan artisan = artisanRepository.findById(artisanId)
                .orElseThrow(() -> ResourceNotFoundException.of("Artisan", artisanId));
        StatutValidation statut = req.statut();
        boolean motifRequis = statut == StatutValidation.REFUSE || statut == StatutValidation.SUSPENDU;
        if (motifRequis && !StringUtils.hasText(req.motif())) {
            throw new BusinessException("Indiquez un motif : il sera communiqué à l'artisan.");
        }

        artisan.setStatutValidation(statut);
        artisan.setMotifRefus(motifRequis ? req.motif().trim() : null);
        if (statut == StatutValidation.VALIDE) {
            artisan.setValideLe(Instant.now());
            notificationService.create(artisan, NotificationType.COMPTE_VALIDE,
                    "Votre compte est validé",
                    "Bienvenue sur Hrayfi ! Publiez votre première annonce pour recevoir des demandes de clients.",
                    "/espace/artisan/annonces/nouvelle");
        }
        return toAdminArtisan(artisan);
    }

    // ---------- utilisateurs ----------

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> utilisateurs(Role role, String q, Pageable pageable) {
        return userRepository.adminSearch(role, like(q), pageable).map(u -> new AdminUserDTO(
                u.getId(), u.getPrenom(), u.getNom(), u.getEmail(), u.getRole(), u.getCreatedAt(),
                u instanceof Artisan a ? a.getStatutValidation() : null));
    }

    // ---------- annonces ----------

    @Transactional(readOnly = true)
    public Page<AnnonceDTO> annonces(String q, Pageable pageable) {
        return annonceRepository.adminSearch(like(q), pageable).map(annonceMapper::toDto);
    }

    public AnnonceDTO changerVisibilite(Long annonceId, boolean active) {
        return annonceService.changerVisibiliteAdmin(annonceId, active);
    }

    public void supprimerAnnonce(Long annonceId) {
        annonceService.supprimerAdmin(annonceId);
    }

    // ---------- avis ----------

    @Transactional(readOnly = true)
    public Page<AvisDTO> avis(Integer noteMax, String q, Pageable pageable) {
        int max = noteMax == null ? 5 : Math.max(1, Math.min(5, noteMax));
        return avisRepository.adminSearch(max, like(q), pageable).map(avisMapper::toDto);
    }

    public void supprimerAvis(Long avisId) {
        avisService.supprimerAdmin(avisId);
    }

    // ---------- demandes ----------

    @Transactional(readOnly = true)
    public Page<DemandeServiceDTO> demandes(StatutDemande statut, Pageable pageable) {
        return demandeRepository.adminSearch(statut, pageable).map(demandeMapper::toDto);
    }

    // ---------- helpers ----------

    private AdminArtisanDTO toAdminArtisan(Artisan a) {
        return new AdminArtisanDTO(
                a.getId(), a.getPrenom(), a.getNom(), a.getEmail(), a.getMetier(), a.getVille(),
                a.getTarifHoraire(), a.getDescription(), a.getCreatedAt(),
                a.getStatutValidation(), a.getMotifRefus(), a.getValideLe(),
                annonceRepository.countByArtisanId(a.getId()));
    }

    /** Motif LIKE en minuscules, ou "" pour ignorer le filtre. */
    private static String like(String q) {
        return StringUtils.hasText(q) ? "%" + q.trim().toLowerCase() + "%" : "";
    }
}
