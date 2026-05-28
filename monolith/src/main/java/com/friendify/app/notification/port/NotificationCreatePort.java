package com.friendify.app.notification.port;

import com.friendify.app.notification.dto.response.NotificationResponse;
import com.friendify.app.shared.notification.NotificationEvent;

public interface NotificationCreatePort {
    NotificationResponse createNotification(
            String userId,
            String type,
            String title,
            String content,
            String relatedUserId,
            String relatedEntityId,
            String relatedEntityType);

    NotificationResponse createNotificationFromEvent(NotificationEvent event);
}
