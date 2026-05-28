package com.friendify.app.notification.mapper;

import com.friendify.app.notification.dto.response.NotificationResponse;
import com.friendify.app.notification.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toNotificationResponse(Notification notification);
}
