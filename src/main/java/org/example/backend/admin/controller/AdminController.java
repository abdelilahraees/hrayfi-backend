package org.example.backend.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.admin.dto.*;
import org.example.backend.admin.service.AdminService;
import org.example.backend.annonce.dto.AnnonceDTO;
import org.example.backend.avis.dto.AvisDTO;
import org.example.backend.demande.dto.DemandeServiceDTO;
import org.example.backend.demande.entity.StatutDemande;
import org.example.backend.paiement.dto.DemandeAbonnementDTO;
import org.example.backend.paiement.dto.EnregistrerPaiementRequest;
import org.example.backend.paiement.dto.TraiterDemandeRequest;
import org.example.backend.paiement.entity.StatutDemandeAbonnement;
import org.example.backend.paiement.service.DemandeAbonnementService;
import org.example.backend.security.AppUserPrincipal;
import org.example.backend.user.entity.Role;
import org.example.backend.user.entity.StatutValidation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "Validation des artisans, comptes, modération des annonces et des avis")
public class AdminController {

    private final AdminService adminService;
    private final DemandeAbonnementService demandeAbonnementService;

    @GetMapping("/stats")
    @Operation(summary = "Chiffres clés de la plateforme")
    public AdminStatsDTO stats() {
        return adminService.stats();
    }

    @GetMapping("/artisans")
    @Operation(summary = "Artisans par statut de validation (EN_ATTENTE pour la file à valider)")
    public Page<AdminArtisanDTO> artisans(@RequestParam(required = false) StatutValidation statut,
                                          @RequestParam(required = false) String q,
                                          @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                          Pageable pageable) {
        return adminService.artisans(statut, q, pageable);
    }

    @PatchMapping("/artisans/{id}/statut")
    @Operation(summary = "Valider, refuser ou suspendre un artisan (motif obligatoire pour un refus ou une suspension)")
    public AdminArtisanDTO changerStatut(@PathVariable Long id, @Valid @RequestBody ChangerStatutRequest req) {
        return adminService.changerStatut(id, req);
    }

    @GetMapping("/comptes")
    @Operation(summary = "Comptes artisans : offre Pro, total payé, mise en avant (filtre TOUS, A_RELANCER, PAYES, OFFERTS, EXPIRES)")
    public Page<AdminCompteDTO> comptes(@RequestParam(defaultValue = "TOUS") String filtre,
                                        @RequestParam(required = false) String q,
                                        @PageableDefault(size = 15, sort = "proJusquAu", direction = Sort.Direction.ASC)
                                        Pageable pageable) {
        return adminService.comptes(filtre, q, pageable);
    }

    @GetMapping("/comptes/{id}")
    @Operation(summary = "Fiche d'un compte : paiements et annonces")
    public AdminCompteDetailDTO compte(@PathVariable Long id) {
        return adminService.compte(id);
    }

    @PostMapping("/comptes/{id}/paiements")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enregistrer un paiement encaissé (CashPlus, Wafacash, virement, espèces) : prolonge le Pro ou met une annonce en avant")
    public AdminCompteDetailDTO enregistrerPaiement(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id,
                                                    @Valid @RequestBody EnregistrerPaiementRequest req) {
        return adminService.enregistrerPaiement(principal.getId(), id, req);
    }

    @PatchMapping("/comptes/{id}/note")
    @Operation(summary = "Notes de suivi de l'équipe sur un artisan (appels, relances)")
    public AdminCompteDTO note(@PathVariable Long id, @Valid @RequestBody NoteAdminRequest req) {
        return adminService.modifierNote(id, req.note());
    }

    @GetMapping("/demandes-offre")
    @Operation(summary = "Demandes d'offre des artisans (EN_ATTENTE, CONTACTEE, PAYEE, REFUSEE, ANNULEE)")
    public Page<DemandeAbonnementDTO> demandesOffre(@RequestParam(required = false) StatutDemandeAbonnement statut,
                                                    @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC)
                                                    Pageable pageable) {
        return demandeAbonnementService.liste(statut, pageable);
    }

    @PatchMapping("/demandes-offre/{id}")
    @Operation(summary = "Décider : contactée, payée (enregistre le paiement), refusée ou suspendre le compte")
    public DemandeAbonnementDTO traiterDemande(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id,
                                               @Valid @RequestBody TraiterDemandeRequest req) {
        return demandeAbonnementService.traiter(principal.getId(), id, req);
    }

    @GetMapping("/utilisateurs")
    @Operation(summary = "Tous les comptes, filtrés par rôle et par texte")
    public Page<AdminUserDTO> utilisateurs(@RequestParam(required = false) Role role,
                                           @RequestParam(required = false) String q,
                                           @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC)
                                           Pageable pageable) {
        return adminService.utilisateurs(role, q, pageable);
    }

    @GetMapping("/annonces")
    @Operation(summary = "Toutes les annonces, en ligne ou masquées")
    public Page<AnnonceDTO> annonces(@RequestParam(required = false) String q,
                                     @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                     Pageable pageable) {
        return adminService.annonces(q, pageable);
    }

    @PatchMapping("/annonces/{id}/visibilite")
    @Operation(summary = "Mettre en ligne ou masquer une annonce")
    public AnnonceDTO visibilite(@PathVariable Long id, @RequestParam boolean active) {
        return adminService.changerVisibilite(id, active);
    }

    @DeleteMapping("/annonces/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer définitivement une annonce et ses photos")
    public void supprimerAnnonce(@PathVariable Long id) {
        adminService.supprimerAnnonce(id);
    }

    @GetMapping("/avis")
    @Operation(summary = "Avis à modérer, filtrés par note maximale et par texte")
    public Page<AvisDTO> avis(@RequestParam(required = false) Integer noteMax,
                              @RequestParam(required = false) String q,
                              @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                              Pageable pageable) {
        return adminService.avis(noteMax, q, pageable);
    }

    @DeleteMapping("/avis/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Retirer un avis (la note de l'annonce est recalculée)")
    public void supprimerAvis(@PathVariable Long id) {
        adminService.supprimerAvis(id);
    }

    @GetMapping("/demandes")
    @Operation(summary = "Toutes les demandes d'intervention, filtrées par statut")
    public Page<DemandeServiceDTO> demandes(@RequestParam(required = false) StatutDemande statut,
                                            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                            Pageable pageable) {
        return adminService.demandes(statut, pageable);
    }
}
