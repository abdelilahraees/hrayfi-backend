package org.example.backend.avis.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.avis.dto.AvisDTO;
import org.example.backend.avis.dto.AvisRequest;
import org.example.backend.avis.service.AvisService;
import org.example.backend.security.AppUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Avis", description = "Notes et commentaires des clients sur les annonces")
public class AvisController {

    private final AvisService avisService;

    @GetMapping("/api/annonces/{annonceId}/avis")
    @Operation(summary = "Avis publics d'une annonce (plus récents d'abord)")
    public Page<AvisDTO> parAnnonce(@PathVariable Long annonceId,
                                    @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                    Pageable pageable) {
        return avisService.listerParAnnonce(annonceId, pageable);
    }

    @GetMapping("/api/artisans/{artisanId}/avis")
    @Operation(summary = "Tous les avis reçus sur les annonces d'un artisan")
    public Page<AvisDTO> parArtisan(@PathVariable Long artisanId,
                                    @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                    Pageable pageable) {
        return avisService.listerParArtisan(artisanId, pageable);
    }

    @PostMapping("/api/annonces/{annonceId}/avis")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Noter et commenter une annonce, ou modifier son avis (CLIENT)")
    public AvisDTO publier(@AuthenticationPrincipal AppUserPrincipal principal,
                           @PathVariable Long annonceId,
                           @Valid @RequestBody AvisRequest body) {
        return avisService.publier(principal.getId(), annonceId, body);
    }

    @GetMapping("/api/avis/mes-avis")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Avis que j'ai laissés (CLIENT)")
    public Page<AvisDTO> mesAvis(@AuthenticationPrincipal AppUserPrincipal principal,
                                 @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                 Pageable pageable) {
        return avisService.mesAvis(principal.getId(), pageable);
    }

    @DeleteMapping("/api/avis/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Supprimer mon avis (CLIENT)")
    public ResponseEntity<Void> supprimer(@AuthenticationPrincipal AppUserPrincipal principal,
                                          @PathVariable Long id) {
        avisService.supprimer(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
