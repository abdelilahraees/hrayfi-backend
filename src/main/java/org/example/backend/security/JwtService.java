package org.example.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.example.backend.user.entity.AppUser;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * Service d'émission et de validation des JWT (HS256).
 * Les claims portent : subject = email, uid = id user, role = rôle applicatif.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
    }

    public String generateToken(AppUser user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.expirationMs());
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.getEmail())
                .claims(Map.of(
                        "uid", user.getId(),
                        "role", user.getRole().name()
                ))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key())
                .compact();
    }

    public Optional<Claims> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public String extractEmail(Claims claims) {
        return claims.getSubject();
    }
}