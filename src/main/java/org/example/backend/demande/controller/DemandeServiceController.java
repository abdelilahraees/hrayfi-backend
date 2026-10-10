package org.example.backend.demande.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.demande.dto.CreateDemandeRequest;
import org.example.backend.demande.dto.DemandeServiceDTO;
import org.example.backend.demande.dto.TerminerDemandeRequest;
import org.example.backend.demande.service.DemandeServiceService;
import org.example.backend.security.AppUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
@Tag(name = "Demandes", description = "Cycle de vie des demandes de service")
public class DemandeServiceController {

    private final DemandeServiceService demandeService;

    // ---------- CLIENT ----------

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Créer une demande (CLIENT)")
    public ResponseEntity<DemandeServiceDTO> creer(@AuthenticationPrincipal AppUserPrincipal principal,
                                                   @Valid @RequestBody CreateDemandeRequest body) {
        DemandeServiceDTO created = demandeService.creerDemande(principal.getId(), body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/mes-demandes")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Lister mes demandes (CLIENT)")
    public Page<DemandeServiceDTO> mesDemandes(@AuthenticationPrincipal AppUserPrincipal principal,
                                               Pageable pageable) {
        return demandeService.listerParClient(principal.getId(), pageable);
    }

    // ---------- ARTISAN ----------

    @GetMapping("/recues")
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Lister les demandes qui me sont adressées (ARTISAN)")
    public Page<DemandeServiceDTO> demandesRecues(@AuthenticationPrincipal AppUserPrincipal principal,
                                                  Pageable pageable) {
        return demandeService.listerParArtisan(principal.getId(), pageable);
    }

    @PatchMapping("/{id}/accepter")
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Accepter une demande (ARTISAN)")
    public DemandeServiceDTO accepter(@AuthenticationPrincipal AppUserPrincipal principal,
                                      @PathVariable Long id) {
        return demandeService.accepterDemande(id, principal.getId());
    }

    @PatchMapping("/{id}/refuser")
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Refuser une demande (ARTISAN)")
    public DemandeServiceDTO refuser(@AuthenticationPrincipal AppUserPrincipal principal,
                                     @PathVariable Long id) {
        return demandeService.refuserDemande(id, principal.getId());
    }

    @PatchMapping("/{id}/terminer")
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Clôturer une demande acceptée avec le prix final (ARTISAN)")
    public DemandeServiceDTO terminer(@AuthenticationPrincipal AppUserPrincipal principal,
                                      @PathVariable Long id,
                                      @Valid @RequestBody TerminerDemandeRequest body) {
        return demandeService.terminerDemande(id, principal.getId(), body.prixFinal());
    }

    // ---------- COMMUN ----------

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Détail d'une demande (client ou artisan concerné)")
    public DemandeServiceDTO detail(@AuthenticationPrincipal AppUserPrincipal principal,
                                    @PathVariable Long id) {
        return demandeService.getById(id, principal.getId());
    }
}