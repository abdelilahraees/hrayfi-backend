package org.example.backend.notification.repository;

import org.example.backend.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByDestinataireIdOrderByCreatedAtDesc(Long destinataireId, Pageable pageable);

    Page<Notification> findByDestinataireIdAndLuFalseOrderByCreatedAtDesc(Long destinataireId, Pageable pageable);

    long countByDestinataireIdAndLuFalse(Long destinataireId);

    /**
     * UPDATE bulk : marque toutes les notifications non lues d'un user comme lues
     * en une seule requête SQL (évite N aller-retours).
     */
    @Modifying
    @Query("UPDATE Notification n SET n.lu = true WHERE n.destinataire.id = :userId AND n.lu = false")
    int markAllAsRead(@Param("userId") Long userId);
}