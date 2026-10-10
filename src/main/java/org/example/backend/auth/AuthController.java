package org.example.backend.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.auth.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Inscription et connexion")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/client")
    @Operation(summary = "Inscription d'un client")
    public ResponseEntity<AuthResponse> registerClient(@Valid @RequestBody RegisterClientRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerClient(req));
    }

    @PostMapping("/register/artisan")
    @Operation(summary = "Inscription d'un artisan")
    public ResponseEntity<AuthResponse> registerArtisan(@Valid @RequestBody RegisterArtisanRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerArtisan(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion → renvoie un JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
