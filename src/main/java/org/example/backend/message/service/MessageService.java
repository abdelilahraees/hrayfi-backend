package org.example.backend.message.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.annonce.repository.AnnonceRepository;
import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.message.dto.ChatPayload;
import org.example.backend.message.dto.ConversationDTO;
import org.example.backend.message.dto.MessageDTO;
import org.example.backend.message.entity.Message;
import org.example.backend.message.mapper.MessageMapper;
import org.example.backend.message.repository.MessageRepository;
import org.example.backend.notification.entity.NotificationType;
import org.example.backend.notification.service.NotificationService;
import org.example.backend.user.entity.AppUser;
import org.example.backend.user.entity.Artisan;
import org.example.backend.user.repository.AppUserRepository;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Messagerie client ⇄ artisan : envoi (REST ou WebSocket), boîte de réception,
 * historique et accusés de lecture. Un message peut contenir une offre de prix
 * liée à une annonce pour négocier avant de créer la demande.
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private static final String QUEUE = "/queue/messages";

    private final MessageRepository messageRepository;
    private final AppUserRepository userRepository;
    private final AnnonceRepository annonceRepository;
    private final MessageMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Transactional
    public MessageDTO envoyer(Long expediteurId, ChatPayload payload) {
        if (expediteurId.equals(payload.destinataireId())) {
            throw new IllegalArgumentException("Impossible de s'envoyer un message à soi-même");
        }
        AppUser expediteur = loadUser(expediteurId);
        AppUser destinataire = loadUser(payload.destinataireId());

        Annonce annonce = null;
        if (payload.annonceId() != null) {
            annonce = annonceRepository.findById(payload.annonceId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Annonce", payload.annonceId()));
            Long auteurId = annonce.getArtisan().getId();
            if (!auteurId.equals(expediteurId) && !auteurId.equals(destinataire.getId())) {
                throw new IllegalArgumentException("L'annonce doit appartenir à l'un des deux interlocuteurs");
            }
        }

        Message saved = messageRepository.save(Message.builder()
                .expediteur(expediteur)
                .destinataire(destinataire)
                .contenu(payload.contenu().trim())
                .annonce(annonce)
                .prixPropose(payload.prixPropose())
                .build());
        MessageDTO dto = mapper.toDto(saved);

        // Destinataire + écho à l'expéditeur (synchronisation multi-onglets), après commit
        pushAfterCommit(List.of(destinataire.getEmail(), expediteur.getEmail()), dto);

        String apercu = payload.prixPropose() != null
                ? "Offre de prix : " + payload.prixPropose() + " MAD"
                : tronquer(payload.contenu(), 80);
        notificationService.create(
                destinataire,
                NotificationType.NOUVEAU_MESSAGE,
                "Nouveau message",
                "%s %s : %s".formatted(expediteur.getPrenom(), expediteur.getNom(), apercu),
                "/messages/" + expediteur.getId()
        );
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> conversation(Long userId, Long otherUserId, Pageable pageable) {
        return messageRepository.findConversation(userId, otherUserId, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> conversations(Long userId) {
        List<Long> lastIds = messageRepository.findLastMessageIdPerConversation(userId);
        if (lastIds.isEmpty()) return List.of();

        Map<Long, Long> nonLus = new HashMap<>();
        for (Object[] row : messageRepository.countUnreadBySender(userId)) {
            nonLus.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }

        return messageRepository.findByIdIn(lastIds).stream()
                .sorted(Comparator.comparing(Message::getTimestamp).reversed())
                .map(m -> {
                    AppUser autre = (AppUser) Hibernate.unproxy(
                            m.getExpediteur().getId().equals(userId) ? m.getDestinataire() : m.getExpediteur());
                    return new ConversationDTO(
                            autre.getId(),
                            (autre.getPrenom() + " " + autre.getNom()).trim(),
                            autre.getRole(),
                            autre instanceof Artisan a ? a.getMetier() : null,
                            m.getContenu(),
                            m.getExpediteur().getId(),
                            m.getPrixPropose(),
                            m.getTimestamp(),
                            nonLus.getOrDefault(autre.getId(), 0L)
                    );
                })
                .toList();
    }

    @Transactional
    public int marquerLu(Long userId, Long otherUserId) {
        return messageRepository.markConversationAsRead(userId, otherUserId);
    }

    @Transactional(readOnly = true)
    public long nonLus(Long userId) {
        return messageRepository.countByDestinataireIdAndLuFalse(userId);
    }

    // ---------- helpers ----------

    private AppUser loadUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", id));
    }

    private void pushAfterCommit(List<String> usernames, MessageDTO dto) {
        Runnable push = () -> usernames.forEach(u -> messagingTemplate.convertAndSendToUser(u, QUEUE, dto));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    push.run();
                }
            });
        } else {
            push.run();
        }
    }

    private static String tronquer(String s, int max) {
        return s.length() > max ? s.substring(0, max - 3) + "…" : s;
    }
}
