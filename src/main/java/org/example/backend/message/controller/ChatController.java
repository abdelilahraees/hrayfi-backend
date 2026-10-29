package org.example.backend.message.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.message.dto.ChatPayload;
import org.example.backend.message.dto.ConversationDTO;
import org.example.backend.message.dto.MessageDTO;
import org.example.backend.message.service.MessageService;
import org.example.backend.security.AppUserPrincipal;
import org.example.backend.user.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * - REST : boîte de réception, historique paginé, envoi, accusés de lecture.
 * - WebSocket STOMP : envoi temps réel sur /app/chat.send. Dans les deux cas le serveur
 *   persiste le message puis le pousse sur la queue privée /user/queue/messages.
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Messagerie et négociation Client ⇄ Artisan")
public class ChatController {

    private final MessageService messageService;
    private final AppUserRepository userRepository;

    // --------- REST ---------

    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")

    @Operation(summary = "Mes conversations, la plus récente d'abord, avec le nombre de non lus")
    public List<ConversationDTO> conversations(@AuthenticationPrincipal AppUserPrincipal principal) {
        return messageService.conversations(principal.getId());
    }

    @GetMapping("/conversation/{otherUserId}")
    @PreAuthorize("isAuthenticated()")

    @Operation(summary = "Historique de la conversation avec un autre utilisateur (plus récents d'abord)")
    public Page<MessageDTO> conversation(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @PathVariable Long otherUserId,
                                         @PageableDefault(size = 50) Pageable pageable) {
        return messageService.conversation(principal.getId(), otherUserId, pageable);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Envoyer un message, éventuellement avec une offre de prix sur une annonce")
    public ResponseEntity<MessageDTO> envoyer(@AuthenticationPrincipal AppUserPrincipal principal,
                                              @Valid @RequestBody ChatPayload body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.envoyer(principal.getId(), body));
    }

    @PatchMapping("/conversation/{otherUserId}/lu")
    @PreAuthorize("isAuthenticated()")

    @Operation(summary = "Marquer comme lus les messages reçus de cet interlocuteur")
    public Map<String, Integer> marquerLu(@AuthenticationPrincipal AppUserPrincipal principal,
                                          @PathVariable Long otherUserId) {
        return Map.of("updated", messageService.marquerLu(principal.getId(), otherUserId));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")

    @Operation(summary = "Nombre total de messages non lus (badge)")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal AppUserPrincipal principal) {
        return Map.of("count", messageService.nonLus(principal.getId()));
    }

    // --------- WebSocket ---------
    // Pas de @PreAuthorize ici : l'identité vient du Principal attaché au CONNECT STOMP.

    @MessageMapping("/chat.send")
    public void envoyerWs(@Payload @Valid ChatPayload payload, Principal principal) {
        Long expediteurId = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", principal.getName()))
                .getId();
        messageService.envoyer(expediteurId, payload);
    }
}
