package org.example.backend.annonce.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.annonce.dto.AnnonceDTO;
import org.example.backend.annonce.dto.AnnonceRequest;
import org.example.backend.annonce.dto.AnnonceSearchCriteria;
import org.example.backend.annonce.dto.MetierCountDTO;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.annonce.entity.AnnonceImage;
import org.example.backend.annonce.mapper.AnnonceMapper;
import org.example.backend.annonce.repository.AnnonceRepository;
import org.example.backend.annonce.repository.AnnonceSpecifications;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.common.exception.UnauthorizedActionException;
import org.example.backend.storage.FileStorageService;
import org.example.backend.user.entity.Artisan;
import org.example.backend.user.repository.ArtisanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Publication et recherche des annonces de services.
 * Seul l'artisan auteur peut modifier, masquer ou supprimer son annonce.
 */
@Service
@RequiredArgsConstructor
public class AnnonceService {

    public static final int MAX_IMAGES = 8;
    private static final String IMAGE_FOLDER = "annonces";

    private final AnnonceRepository annonceRepository;
    private final ArtisanRepository artisanRepository;
    private final AnnonceMapper mapper;
    private final FileStorageService storageService;

    // ---------- lecture publique ----------

    @Transactional(readOnly = true)
    public Page<AnnonceDTO> rechercher(AnnonceSearchCriteria criteria, Pageable pageable) {
        return annonceRepository.findAll(AnnonceSpecifications.search(criteria), pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public AnnonceDTO getPublic(Long id, Long currentUserId) {
        Annonce annonce = load(id);
        boolean estAuteur = annonce.getArtisan().getId().equals(currentUserId);
        if (!annonce.isActive() && !estAuteur) {
            throw ResourceNotFoundException.of("Annonce", id);
        }
        return mapper.toDto(annonce);
    }

    @Transactional(readOnly = true)
    public Page<AnnonceDTO> listerParArtisan(Long artisanId, Pageable pageable) {
        return annonceRepository.findByArtisanIdAndActiveTrue(artisanId, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<MetierCountDTO> metiers() {
        return annonceRepository.countByMetier();
    }

    // ---------- espace artisan ----------

    @Transactional(readOnly = true)
    public Page<AnnonceDTO> mesAnnonces(Long artisanId, Pageable pageable) {
        return annonceRepository.findByArtisanId(artisanId, pageable).map(mapper::toDto);
    }

    @Transactional
    public AnnonceDTO creer(Long artisanId, AnnonceRequest req, List<MultipartFile> images) {
        Artisan artisan = artisanRepository.findById(artisanId)
                .orElseThrow(() -> ResourceNotFoundException.of("Artisan", artisanId));
        validerPrix(req);

        Annonce annonce = Annonce.builder().artisan(artisan).build();
        appliquer(annonce, req, artisan);
        ajouterImages(annonce, images);
        return mapper.toDto(annonceRepository.save(annonce));
    }

    @Transactional
    public AnnonceDTO modifier(Long annonceId, Long artisanId, AnnonceRequest req) {
        Annonce annonce = loadForArtisan(annonceId, artisanId);
        validerPrix(req);
        appliquer(annonce, req, annonce.getArtisan());
        return mapper.toDto(annonce);
    }

    @Transactional
    public AnnonceDTO changerVisibilite(Long annonceId, Long artisanId, boolean active) {
        Annonce annonce = loadForArtisan(annonceId, artisanId);
        annonce.setActive(active);
        return mapper.toDto(annonce);
    }

    @Transactional
    public AnnonceDTO ajouterImages(Long annonceId, Long artisanId, List<MultipartFile> images) {
        Annonce annonce = loadForArtisan(annonceId, artisanId);
        ajouterImages(annonce, images);
        return mapper.toDto(annonceRepository.saveAndFlush(annonce));
    }

    @Transactional
    public AnnonceDTO supprimerImage(Long annonceId, Long imageId, Long artisanId) {
        Annonce annonce = loadForArtisan(annonceId, artisanId);
        AnnonceImage image = annonce.getImages().stream()
                .filter(i -> i.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> ResourceNotFoundException.of("Image", imageId));

        annonce.getImages().remove(image);
        for (int i = 0; i < annonce.getImages().size(); i++) {
            annonce.getImages().get(i).setPosition(i);
        }
        storageService.delete(image.getUrl());
        return mapper.toDto(annonce);
    }

    @Transactional
    public void supprimer(Long annonceId, Long artisanId) {
        Annonce annonce = loadForArtisan(annonceId, artisanId);
        List<String> urls = annonce.getImages().stream().map(AnnonceImage::getUrl).toList();
        annonceRepository.delete(annonce);
        urls.forEach(storageService::delete);
    }

    // ---------- helpers ----------

    private void appliquer(Annonce annonce, AnnonceRequest req, Artisan artisan) {
        annonce.setTitre(req.titre().trim());
        annonce.setDescription(req.description().trim());
        annonce.setMetier(StringUtils.hasText(req.metier()) ? req.metier().trim() : artisan.getMetier());
        annonce.setVille(StringUtils.hasText(req.ville()) ? req.ville().trim() : artisan.getVille());
        annonce.setPrixMin(req.prixMin());
        annonce.setPrixMax(req.prixMax());
    }

    private void ajouterImages(Annonce annonce, List<MultipartFile> images) {
        if (images == null) return;
        List<MultipartFile> nonVides = images.stream().filter(f -> f != null && !f.isEmpty()).toList();
        if (annonce.getImages().size() + nonVides.size() > MAX_IMAGES) {
            throw new BusinessException("Une annonce peut contenir au maximum " + MAX_IMAGES + " photos");
        }
        nonVides.forEach(f -> annonce.addImage(storageService.storeImage(f, IMAGE_FOLDER)));
    }

    private void validerPrix(AnnonceRequest req) {
        if (req.prixMin() != null && req.prixMax() != null && req.prixMin().compareTo(req.prixMax()) > 0) {
            throw new IllegalArgumentException("Le prix minimum doit être inférieur ou égal au prix maximum");
        }
    }

    private Annonce load(Long id) {
        return annonceRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Annonce", id));
    }

    private Annonce loadForArtisan(Long annonceId, Long artisanId) {
        Annonce annonce = load(annonceId);
        if (!annonce.getArtisan().getId().equals(artisanId)) {
            throw new UnauthorizedActionException("Cette annonce ne vous appartient pas");
        }
        return annonce;
    }
}
