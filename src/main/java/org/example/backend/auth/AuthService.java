package org.example.backend.auth;

import lombok.RequiredArgsConstructor;
import org.example.backend.auth.dto.*;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.security.AppUserPrincipal;
import org.example.backend.security.JwtProperties;
import org.example.backend.security.JwtService;
import org.example.backend.user.entity.*;
import org.example.backend.user.repository.AppUserRepository;
import org.example.backend.user.repository.ArtisanRepository;
import org.example.backend.user.repository.ClientRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ArtisanRepository artisanRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse registerClient(RegisterClientRequest req) {
        guardEmailAvailable(req.email());

        Client client = Client.builder()
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .nom(req.nom())
                .prenom(req.prenom())
                .role(Role.CLIENT)
                .adresse(req.adresse())
                .preferences(req.preferences())
                .build();

        Client saved = clientRepository.save(client);
        return buildResponse(saved);
    }

    @Transactional
    public AuthResponse registerArtisan(RegisterArtisanRequest req) {
        guardEmailAvailable(req.email());

        Artisan artisan = Artisan.builder()
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .nom(req.nom())
                .prenom(req.prenom())
                .role(Role.ARTISAN)
                .metier(req.metier())
                .tarifHoraire(req.tarifHoraire())
                .description(req.description())
                .ville(req.ville())
                .disponibilite(Disponibilite.DISPONIBLE)
                .build();

        Artisan saved = artisanRepository.save(artisan);
        return buildResponse(saved);
    }

    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );
        AppUser user = ((AppUserPrincipal) auth.getPrincipal()).getUser();
        return buildResponse(user);
    }

    private void guardEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email déjà utilisé : " + email);
        }
    }

    private AuthResponse buildResponse(AppUser user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(
                token,
                "Bearer",
                jwtProperties.expirationMs(),
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }
}
