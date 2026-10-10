package org.example.backend.evaluation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.evaluation.dto.CreateEvaluationRequest;
import org.example.backend.evaluation.dto.EvaluationDTO;
import org.example.backend.evaluation.service.EvaluationService;
import org.example.backend.security.AppUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Évaluations", description = "Notation + commentaires des demandes terminées")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping("/api/demandes/{demandeId}/evaluation")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Noter une demande TERMINEE (CLIENT)")
    public ResponseEntity<EvaluationDTO> evaluer(@AuthenticationPrincipal AppUserPrincipal principal,
                                                 @PathVariable Long demandeId,
                                                 @Valid @RequestBody CreateEvaluationRequest req) {
        EvaluationDTO created = evaluationService.evaluer(principal.getId(), demandeId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/api/artisans/{artisanId}/evaluations")
    @Operation(summary = "Liste publique des évaluations d'un artisan")
    public Page<EvaluationDTO> listerPourArtisan(@PathVariable Long artisanId, Pageable pageable) {
        return evaluationService.listerParArtisan(artisanId, pageable);
    }
}
