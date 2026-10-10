package org.example.backend.notification.dto;

import org.example.backend.notification.entity.NotificationType;

import java.time.Instant;

public record NotificationDTO(
        Long id,
        NotificationType type,
        String titre,
        String message,
        Boolean lu,
        String lienCible,
        Instant createdAt
) {}
