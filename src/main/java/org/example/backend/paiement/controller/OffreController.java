package org.example.backend.paiement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.paiement.dto.CreerDemandeAbonnementRequest;
import org.example.backend.paiement.dto.DemandeAbonnementDTO;
import org.example.backend.paiement.dto.FormuleDTO;
import org.example.backend.paiement.dto.OffreDTO;
import org.example.backend.paiement.service.DemandeAbonnementService;
import org.example.backend.paiement.service.OffreService;
import org.example.backend.security.AppUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Offre Pro", description = "Situation de l'offre Pro et paiements de l'artisan connecté")
public class OffreController {

    private final OffreService offreService;
    private final DemandeAbonnementService demandeService;

    @GetMapping("/api/offres/formules")
    @Operation(summary = "Catalogue des offres : prix, durée et ce que chacune apporte")
    public List<FormuleDTO> formules() {
        return demandeService.formules();
    }

    @PostMapping("/api/artisans/me/demandes-offre")
    @PreAuthorize("hasRole('ARTISAN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Demander une offre (Pro ou mise en avant) : l'équipe est notifiée et recontacte l'artisan")
    public DemandeAbonnementDTO demander(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @Valid @RequestBody CreerDemandeAbonnementRequest req) {
        return demandeService.creer(principal.getId(), req);
    }

    @GetMapping("/api/artisans/me/demandes-offre")
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Mes demandes d'offre et les réponses de l'équipe")
    public List<DemandeAbonnementDTO> mesDemandes(@AuthenticationPrincipal AppUserPrincipal principal) {
        return demandeService.mesDemandes(principal.getId());
    }

    @PatchMapping("/api/artisans/me/demandes-offre/{id}/annuler")
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Annuler une demande d'offre pas encore traitée")
    public DemandeAbonnementDTO annuler(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        return demandeService.annuler(principal.getId(), id);
    }

    @GetMapping("/api/artisans/me/offre")
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Mon offre Pro, mes annonces mises en avant et mes paiements")
    public OffreDTO monOffre(@AuthenticationPrincipal AppUserPrincipal principal) {
        return offreService.monOffre(principal.getId());
    }
}
