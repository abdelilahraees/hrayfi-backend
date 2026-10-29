package org.example.backend.user.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.annonce.repository.AnnonceRepository;
import org.example.backend.avis.repository.AvisAnnonceRepository;
import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.common.util.AggregateRows;
import org.example.backend.user.dto.ArtisanPublicDTO;
import org.example.backend.user.dto.MeDTO;
import org.example.backend.user.dto.UpdateProfileRequest;
import org.example.backend.user.entity.AppUser;
import org.example.backend.user.entity.Artisan;
import org.example.backend.user.entity.Client;
import org.example.backend.user.repository.AppUserRepository;
import org.example.backend.user.repository.ArtisanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository userRepository;
    private final ArtisanRepository artisanRepository;
    private final AnnonceRepository annonceRepository;
    private final AvisAnnonceRepository avisRepository;

    // ---------- profil connecté ----------

    @Transactional(readOnly = true)
    public MeDTO me(Long userId) {
        return toMe(loadUser(userId));
    }

    @Transactional
    public MeDTO updateMe(Long userId, UpdateProfileRequest req) {
        AppUser user = loadUser(userId);
        if (req.nom() != null) user.setNom(req.nom().trim());
        if (req.prenom() != null) user.setPrenom(req.prenom().trim());

        if (user instanceof Client c) {
            if (req.adresse() != null) c.setAdresse(req.adresse().trim());
            if (req.preferences() != null) c.setPreferences(req.preferences().trim());
        } else if (user instanceof Artisan a) {
            if (req.metier() != null) a.setMetier(req.metier().trim());
            if (req.tarifHoraire() != null) a.setTarifHoraire(req.tarifHoraire());
            if (req.description() != null) a.setDescription(req.description().trim());
            if (req.ville() != null) a.setVille(req.ville().trim());
            if (req.disponibilite() != null) a.setDisponibilite(req.disponibilite());
        }
        return toMe(user);
    }

    // ---------- annuaire public des artisans ----------

    @Transactional(readOnly = true)
    public Page<ArtisanPublicDTO> rechercherArtisans(String metier, String ville, String q, Pageable pageable) {
        return artisanRepository.search(normalize(metier), normalize(ville), like(q), pageable)
                .map(this::toPublic);
    }

    @Transactional(readOnly = true)
    public ArtisanPublicDTO artisanPublic(Long id) {
        return artisanRepository.findById(id)
                .map(this::toPublic)
                .orElseThrow(() -> ResourceNotFoundException.of("Artisan", id));
    }

    // ---------- helpers ----------

    private ArtisanPublicDTO toPublic(Artisan a) {
        Object[] stats = avisRepository.statsForArtisan(a.getId());
        double moyenne = Math.round(AggregateRows.average(stats) * 100.0) / 100.0;
        return new ArtisanPublicDTO(
                a.getId(), a.getNom(), a.getPrenom(), a.getMetier(), a.getTarifHoraire(),
                a.getDescription(), a.getVille(), a.getDisponibilite(),
                moyenne, AggregateRows.count(stats),
                annonceRepository.countByArtisanIdAndActiveTrue(a.getId()),
                a.getCreatedAt()
        );
    }

    private MeDTO toMe(AppUser u) {
        Client c = u instanceof Client client ? client : null;
        Artisan a = u instanceof Artisan artisan ? artisan : null;
        return new MeDTO(
                u.getId(), u.getEmail(), u.getNom(), u.getPrenom(), u.getRole(), u.getCreatedAt(),
                c != null ? c.getAdresse() : null,
                c != null ? c.getPreferences() : null,
                a != null ? a.getMetier() : null,
                a != null ? a.getTarifHoraire() : null,
                a != null ? a.getDescription() : null,
                a != null ? a.getVille() : null,
                a != null ? a.getDisponibilite() : null
        );
    }

    private AppUser loadUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", id));
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static String like(String s) {
        String n = normalize(s);
        return n.isEmpty() ? "" : "%" + n + "%";
    }
}
