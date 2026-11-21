package org.example.backend.paiement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.paiement.dto.OffreDTO;
import org.example.backend.paiement.service.OffreService;
import org.example.backend.security.AppUserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Offre Pro", description = "Situation de l'offre Pro et paiements de l'artisan connecté")
public class OffreController {

    private final OffreService offreService;

    @GetMapping("/api/artisans/me/offre")
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Mon offre Pro, mes annonces mises en avant et mes paiements")
    public OffreDTO monOffre(@AuthenticationPrincipal AppUserPrincipal principal) {
        return offreService.monOffre(principal.getId());
    }
}
