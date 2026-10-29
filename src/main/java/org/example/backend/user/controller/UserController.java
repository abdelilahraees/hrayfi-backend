package org.example.backend.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.security.AppUserPrincipal;
import org.example.backend.user.dto.ArtisanPublicDTO;
import org.example.backend.user.dto.MeDTO;
import org.example.backend.user.dto.UpdateProfileRequest;
import org.example.backend.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/api/artisans/{id:\\d+}")
    @Operation(summary = "Profil public d'un artisan")
    public ArtisanPublicDTO artisan(@PathVariable Long id) {
        return userService.artisanPublic(id);
    }
}
