package org.example.backend.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.geo.VillesMaroc;
import org.example.backend.security.AppUserPrincipal;
import org.example.backend.user.dto.ArtisanProcheDTO;
import org.example.backend.user.dto.ArtisanPublicDTO;
import org.example.backend.user.dto.ContactArtisanDTO;
import org.example.backend.user.dto.MeDTO;
import org.example.backend.user.dto.UpdateProfileRequest;
import org.example.backend.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "Profil connecté et annuaire public des artisans")
public class UserController {

    private final UserService userService;

    @GetMapping("/api/users/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Profil de l'utilisateur connecté")
    public MeDTO me(@AuthenticationPrincipal AppUserPrincipal principal) {
        return userService.me(principal.getId());
    }

    @PutMapping("/api/users/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mettre à jour mon profil (champs null = inchangés)")
    public MeDTO updateMe(@AuthenticationPrincipal AppUserPrincipal principal,
                          @Valid @RequestBody UpdateProfileRequest body) {
        return userService.updateMe(principal.getId(), body);
    }

    @GetMapping("/api/artisans")
    @Operation(summary = "Rechercher des artisans (métier, ville, mot-clé)")
    public Page<ArtisanPublicDTO> rechercher(@RequestParam(required = false) String metier,
                                             @RequestParam(required = false) String ville,
                                             @RequestParam(required = false) String q,
                                             @PageableDefault(size = 12) Pageable pageable) {
        return userService.rechercherArtisans(metier, ville, q, pageable);
    }

    @GetMapping("/api/artisans/proches")
    @Operation(summary = "Artisans validés autour d'une position, triés par distance")
    public List<ArtisanProcheDTO> proches(@RequestParam double lat,
                                          @RequestParam double lng,
                                          @RequestParam(defaultValue = "25") double rayon,
                                          @RequestParam(required = false) String metier,
                                          @RequestParam(defaultValue = "50") int limite) {
        return userService.artisansProches(lat, lng, rayon, metier, limite);
    }

    @GetMapping("/api/artisans/villes")
    @Operation(summary = "Villes et coordonnées de leur centre, pour choisir un point de départ sans géolocalisation")
    public List<VillesMaroc.Ville> villes() {
        return VillesMaroc.toutes();
    }

    @GetMapping("/api/artisans/{id:\\d+}/contact")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Téléphone d'un artisan validé (connecté uniquement, pour WhatsApp ou un appel)")
    public ContactArtisanDTO contact(@PathVariable Long id) {
        return userService.contactArtisan(id);
    }

    @GetMapping("/api/artisans/{id:\\d+}")
    @Operation(summary = "Profil public d'un artisan")
    public ArtisanPublicDTO artisan(@PathVariable Long id) {
        return userService.artisanPublic(id);
    }
}
