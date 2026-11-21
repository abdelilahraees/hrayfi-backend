package org.example.backend.auth;

import org.example.backend.common.exception.UnauthorizedActionException;
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
        // Pas de connexion automatique : le compte doit d'abord être validé par un administrateur
        return new AuthResponse(null, "Bearer", 0, saved.getId(), saved.getEmail(), saved.getRole(), saved.getStatutValidation());
    }

    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );
        AppUserPrincipal principal = (AppUserPrincipal) auth.getPrincipal();
        if (!principal.isActif()) {
            throw new UnauthorizedActionException(messageCompteInactif((Artisan) principal.getUser()));
        }
        return buildResponse(principal.getUser());
    }

    private String messageCompteInactif(Artisan artisan) {
        String brut = artisan.getMotifRefus() == null ? "" : artisan.getMotifRefus().trim();
        String motif = brut.isEmpty() ? "" : " Motif : " + brut + (brut.matches(".*[.!?]$") ? "" : ".");
        return switch (artisan.getStatutValidation()) {
            case EN_ATTENTE -> "Votre compte artisan est en cours de vérification par l'équipe Hrayfi (24 à 48 h en général). "
                    + "Vous pourrez vous connecter dès sa validation.";
            case REFUSE -> "Votre demande de compte artisan n'a pas été acceptée." + motif
                    + " Pour plus d'informations, écrivez à contact@hrayfi.ma.";
            case SUSPENDU -> "Votre compte artisan est suspendu." + motif + " Contactez contact@hrayfi.ma pour le réactiver.";
            case VALIDE -> "Compte actif";
        };
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
                user.getRole(),
                user instanceof Artisan a ? a.getStatutValidation() : null
        );
    }
}
