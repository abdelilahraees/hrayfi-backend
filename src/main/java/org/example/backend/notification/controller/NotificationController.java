package org.example.backend.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.common.exception.UnauthorizedActionException;
import org.example.backend.notification.dto.NotificationDTO;
import org.example.backend.notification.entity.Notification;
import org.example.backend.notification.mapper.NotificationMapper;
import org.example.backend.notification.repository.NotificationRepository;
import org.example.backend.security.AppUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Notifications", description = "Centre de notifications utilisateur")
public class NotificationController {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    @GetMapping
    @Operation(summary = "Mes notifications (triées par date desc)")
    public Page<NotificationDTO> list(@AuthenticationPrincipal AppUserPrincipal principal,
                                      @RequestParam(defaultValue = "false") boolean onlyUnread,
                                      Pageable pageable) {
        Page<Notification> page = onlyUnread
                ? repository.findByDestinataireIdAndLuFalseOrderByCreatedAtDesc(principal.getId(), pageable)
                : repository.findByDestinataireIdOrderByCreatedAtDesc(principal.getId(), pageable);
        return page.map(mapper::toDto);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Nombre de notifications non lues (pour badge UI)")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal AppUserPrincipal principal) {
        return Map.of("count", repository.countByDestinataireIdAndLuFalse(principal.getId()));
    }

    @PatchMapping("/{id}/lu")
    @Transactional
    @Operation(summary = "Marquer une notification comme lue")
    public NotificationDTO markRead(@AuthenticationPrincipal AppUserPrincipal principal,
                                    @PathVariable Long id) {
        Notification n = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", id));
        if (!n.getDestinataire().getId().equals(principal.getId())) {
            throw new UnauthorizedActionException("Cette notification ne vous appartient pas");
        }
        n.setLu(true);
        return mapper.toDto(n);
    }

    @PatchMapping("/mark-all-read")
    @Transactional
    @Operation(summary = "Marquer toutes mes notifications comme lues")
    public Map<String, Integer> markAllRead(@AuthenticationPrincipal AppUserPrincipal principal) {
        int updated = repository.markAllAsRead(principal.getId());
        return Map.of("updated", updated);
    }
}
