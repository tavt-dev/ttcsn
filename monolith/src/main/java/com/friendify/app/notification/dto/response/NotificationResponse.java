package com.friendify.app.notification.dto.response;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    String id;
    String userId;
    String type;
    String title;
    String content;
    String relatedUserId;
    String relatedEntityId;
    String relatedEntityType;
    Boolean isRead;
    Instant createdAt;
    Instant readAt;
}
