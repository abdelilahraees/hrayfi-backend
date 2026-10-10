package org.example.backend.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.dashboard.dto.ArtisanDashboardDTO;
import org.example.backend.dashboard.dto.ClientDashboardDTO;
import org.example.backend.dashboard.service.DashboardService;
import org.example.backend.security.AppUserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Dashboards", description = "Agrégats pour les écrans d'accueil")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/artisans/me/dashboard")
    @PreAuthorize("hasRole('ARTISAN')")
    @Operation(summary = "Tableau de bord de l'artisan connecté")
    public ArtisanDashboardDTO artisanDashboard(@AuthenticationPrincipal AppUserPrincipal principal) {
        return dashboardService.dashboardArtisan(principal.getId());
    }

    @GetMapping("/clients/me/dashboard")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Tableau de bord du client connecté")
    public ClientDashboardDTO clientDashboard(@AuthenticationPrincipal AppUserPrincipal principal) {
        return dashboardService.dashboardClient(principal.getId());
    }
}
