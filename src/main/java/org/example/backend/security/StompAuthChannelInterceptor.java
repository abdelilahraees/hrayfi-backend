package org.example.backend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Authentifie les sessions STOMP sur la frame CONNECT :
 *   - lit l'en-tête `Authorization: Bearer <jwt>`
 *   - valide le token
 *   - attache l'Authentication à l'accessor → disponible dans les @MessageMapping
 *     et nécessaire pour que convertAndSendToUser(email,…) route correctement.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String header = accessor.getFirstNativeHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            log.debug("STOMP CONNECT sans Authorization — session anonyme");
            return message;
        }

        String token = header.substring(PREFIX.length());
        jwtService.parse(token).ifPresent(claims -> {
            AppUserPrincipal principal =
                    (AppUserPrincipal) userDetailsService.loadUserByUsername(jwtService.extractEmail(claims));
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            accessor.setUser(auth);
        });
        return message;
    }
}