package com.friendify.app.chat.dto.request;

import com.friendify.app.chat.enums.ChatNotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatNotification {
    String conversationId;
    String sender;
    String content;
    ChatNotificationType type;
}
