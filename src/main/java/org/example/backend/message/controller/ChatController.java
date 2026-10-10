package org.example.backend.message.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.message.dto.ChatPayload;
import org.example.backend.message.dto.MessageDTO;
import org.example.backend.message.entity.Message;
import org.example.backend.message.mapper.MessageMapper;
import org.example.backend.message.repository.MessageRepository;
import org.example.backend.notification.entity.NotificationType;
import org.example.backend.notification.service.NotificationService;
import org.example.backend.security.AppUserPrincipal;
import org.example.backend.user.entity.AppUser;
import org.example.backend.user.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * - REST : historique d'une conversation (pagination).
 * - WebSocket STOMP : envoi en temps réel. Le serveur persiste le message
 *   puis le pousse à la queue privée du destinataire (/user/queue/messages).
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Messagerie Client ⇄ Artisan")
public class ChatController {

    private final MessageRepository messageRepository;
    private final AppUserRepository userRepository;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    // --------- REST : historique ---------

    @GetMapping("/conversation/{otherUserId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Historique de la conversation avec un autre utilisateur")
    public Page<MessageDTO> conversation(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @PathVariable Long otherUserId,
                                         Pageable pageable) {
        return messageRepository.findConversation(principal.getId(), otherUserId, pageable)
                .map(messageMapper::toDto);
    }

    // --------- WebSocket : envoi temps réel ---------

    @MessageMapping("/chat.send")
    @Transactional
    public void envoyer(@Payload @Valid ChatPayload payload, Principal principal) {
        AppUser expediteur = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", principal.getName()));
        AppUser destinataire = userRepository.findById(payload.destinataireId())
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", payload.destinataireId()));

        Message saved = messageRepository.save(Message.builder()
                .expediteur(expediteur)
                .destinataire(destinataire)
                .contenu(payload.contenu())
                .build());

        MessageDTO dto = messageMapper.toDto(saved);

        // Pousse vers la queue privée du destinataire (abonnement front : /user/queue/messages)
        messagingTemplate.convertAndSendToUser(
                destinataire.getEmail(), "/queue/messages", dto);

        // Echo à l'expéditeur pour synchronisation multi-onglets
        messagingTemplate.convertAndSendToUser(
                expediteur.getEmail(), "/queue/messages", dto);

        notificationService.create(
                destinataire,
                NotificationType.NOUVEAU_MESSAGE,
                "Nouveau message",
                "%s %s : %s".formatted(expediteur.getPrenom(), expediteur.getNom(),
                        payload.contenu().length() > 80
                                ? payload.contenu().substring(0, 77) + "…"
                                : payload.contenu()),
                "/messages/" + expediteur.getId()
        );
    }
}