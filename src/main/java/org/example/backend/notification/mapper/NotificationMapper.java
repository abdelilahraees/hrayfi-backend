package org.example.backend.notification.mapper;

import org.example.backend.notification.dto.NotificationDTO;
import org.example.backend.notification.entity.Notification;
import org.mapstruct.Mapper;

@Mapper
public interface NotificationMapper {
    NotificationDTO toDto(Notification notification);
}
