package org.example.backend.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.notification.dto.NotificationDTO;
import org.example.backend.notification.entity.Notification;
import org.example.backend.notification.entity.NotificationType;
import org.example.backend.notification.mapper.NotificationMapper;
import org.example.backend.notification.repository.NotificationRepository;
import org.example.backend.user.entity.AppUser;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Création + push temps réel des notifications.
 *
 *  - Persistée en DB → consultable via GET /api/notifications (badge "non lues", historique).
 *  - Poussée via STOMP sur /user/queue/notifications → le front qui a un abonnement actif
 *    la reçoit instantanément.
 *
 * Le push WebSocket est différé APRÈS commit de la transaction pour éviter d'envoyer
 * une notif qui référence une donnée que la DB n'a pas encore commitée (ex: rollback).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final String DESTINATION = "/queue/notifications";

    private final NotificationRepository repository;
    private final NotificationMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public NotificationDTO create(AppUser destinataire, NotificationType type,
                                   String titre, String message, String lienCible) {
        Notification entity = repository.save(Notification.builder()
                .destinataire(destinataire)
                .type(type)
                .titre(titre)
                .message(message)
                .lienCible(lienCible)
                .lu(false)
                .build());

        NotificationDTO dto = mapper.toDto(entity);
        pushAfterCommit(destinataire.getEmail(), dto);
        return dto;
    }

    private void pushAfterCommit(String username, NotificationDTO dto) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    safePush(username, dto);
                }
            });
        } else {
            safePush(username, dto);
        }
    }

    private void safePush(String username, NotificationDTO dto) {
        try {
            messagingTemplate.convertAndSendToUser(username, DESTINATION, dto);
        } catch (Exception ex) {
            // Un échec de push ne doit pas casser le flux métier : la notif reste en DB.
            log.warn("Push WebSocket échoué pour {} : {}", username, ex.getMessage());
        }
    }
}
