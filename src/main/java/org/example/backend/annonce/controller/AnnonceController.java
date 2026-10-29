package org.example.backend.annonce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.annonce.dto.AnnonceDTO;
import org.example.backend.annonce.dto.AnnonceRequest;
import org.example.backend.annonce.dto.AnnonceSearchCriteria;
import org.example.backend.annonce.dto.MetierCountDTO;
import org.example.backend.annonce.service.AnnonceService;
import org.example.backend.security.AppUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/annonces")
@RequiredArgsConstructor
@Tag(name = "Annonces", description = "Services publiés par les artisans (photos, description, prix)")
public class AnnonceController {

    private final AnnonceService annonceService;

    // ---------- PUBLIC ----------

    @GetMapping
    @Operation(summary = "Rechercher / filtrer les annonces (tri : noteMoyenne, createdAt, prixMin)")
    public Page<AnnonceDTO> rechercher(AnnonceSearchCriteria criteria,
                                       @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
                                       Pageable pageable) {
        return annonceService.rechercher(criteria, pageable);
    }

    @GetMapping("/metiers")
    @Operation(summary = "Métiers ayant au moins une annonce, avec leur nombre d'annonces")
    public List<MetierCountDTO> metiers() {
        return annonceService.metiers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une annonce")
    public AnnonceDTO detail(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        return annonceService.getPublic(id, principal == null ? null : principal.getId());
    }

    @GetMapping("/artisan/{artisanId}")
    @Operation(summary = "Annonces visibles d'un artisan")
    public Page<AnnonceDTO> parArtisan(@PathVariable Long artisanId,
                                       @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
                                       Pageable pageable) {
        return annonceService.listerParArtisan(artisanId, pageable);
    }

    // ---------- ARTISAN ----------

    @GetMapping("/mes-annonces")
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Mes annonces, visibles ou masquées (ARTISAN)")
    public Page<AnnonceDTO> mesAnnonces(@AuthenticationPrincipal AppUserPrincipal principal,
                                        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                                        Pageable pageable) {
        return annonceService.mesAnnonces(principal.getId(), pageable);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Publier une annonce avec ses photos (ARTISAN) — parts : data (JSON) + images")
    public ResponseEntity<AnnonceDTO> creer(@AuthenticationPrincipal AppUserPrincipal principal,
                                            @Valid @RequestPart("data") AnnonceRequest data,
                                            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        AnnonceDTO created = annonceService.creer(principal.getId(), data, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Modifier le texte et les prix d'une annonce (ARTISAN)")
    public AnnonceDTO modifier(@AuthenticationPrincipal AppUserPrincipal principal,
                               @PathVariable Long id,
                               @Valid @RequestBody AnnonceRequest body) {
        return annonceService.modifier(id, principal.getId(), body);
    }

    @PatchMapping("/{id}/visibilite")
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Afficher ou masquer une annonce (ARTISAN) — body : {\"active\": true|false}")
    public AnnonceDTO visibilite(@AuthenticationPrincipal AppUserPrincipal principal,
                                 @PathVariable Long id,
                                 @RequestBody Map<String, Boolean> body) {
        return annonceService.changerVisibilite(id, principal.getId(), Boolean.TRUE.equals(body.get("active")));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Ajouter des photos à une annonce (ARTISAN)")
    public AnnonceDTO ajouterImages(@AuthenticationPrincipal AppUserPrincipal principal,
                                    @PathVariable Long id,
                                    @RequestPart("images") List<MultipartFile> images) {
        return annonceService.ajouterImages(id, principal.getId(), images);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Retirer une photo d'une annonce (ARTISAN)")
    public AnnonceDTO supprimerImage(@AuthenticationPrincipal AppUserPrincipal principal,
                                     @PathVariable Long id,
                                     @PathVariable Long imageId) {
        return annonceService.supprimerImage(id, imageId, principal.getId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Supprimer une annonce (ARTISAN)")
    public ResponseEntity<Void> supprimer(@AuthenticationPrincipal AppUserPrincipal principal,
                                          @PathVariable Long id) {
        annonceService.supprimer(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
