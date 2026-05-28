package com.friendify.app.chat.controller;

import java.security.Principal;

import com.friendify.app.chat.dto.request.ChatMessageRequest;
import com.friendify.app.chat.dto.request.ChatNotification;
import com.friendify.app.chat.dto.request.TypingNotification;
import com.friendify.app.chat.dto.response.ChatMessageResponse;
import com.friendify.app.chat.enums.ChatNotificationType;
import com.friendify.app.chat.repository.ConversationRepository;
import com.friendify.app.chat.service.ChatMessageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSocketController {

    ChatMessageService chatMessageService;
    SimpMessagingTemplate messagingTemplate;
    ConversationRepository conversationRepository;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        String userId = null;
        try {
            if (principal == null || principal.getName() == null) {
                return;
            }
            userId = principal.getName();
            if (principal instanceof Authentication authentication) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            ChatMessageResponse response = chatMessageService.create(request);
            response.setMe(false);
            messagingTemplate.convertAndSend("/topic/conversation/" + request.getConversationId(), response);
        } catch (Exception exception) {
            log.error("Failed to send WebSocket chat message", exception);
            if (userId != null) {
                messagingTemplate.convertAndSendToUser(userId, "/queue/errors", "Send message failed");
            }
        }
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingNotification notification, Principal principal) {
        try {
            if (principal == null || principal.getName() == null || !hasText(notification.getConversationId())) {
                return;
            }
            String userId = principal.getName();
            if (!isParticipant(notification.getConversationId(), userId)) {
                return;
            }
            notification.setUserId(userId);
            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + notification.getConversationId() + "/typing", notification);
        } catch (Exception exception) {
            log.error("Failed to handle WebSocket typing notification", exception);
        }
    }

    @MessageMapping("/chat.addUser")
    public void handleUserJoin(@Payload ChatNotification notification, Principal principal) {
        try {
            if (principal == null || principal.getName() == null || !hasText(notification.getConversationId())) {
                return;
            }
            String userId = principal.getName();
            if (!isParticipant(notification.getConversationId(), userId)) {
                return;
            }
            notification.setSender(userId);
            if (notification.getType() == null) {
                notification.setType(ChatNotificationType.JOIN);
            }
            messagingTemplate.convertAndSend("/topic/conversation/" + notification.getConversationId(), notification);
        } catch (Exception exception) {
            log.error("Failed to handle WebSocket user join", exception);
        }
    }

    @MessageMapping("/chat.removeUser")
    public void handleUserLeave(@Payload ChatNotification notification, Principal principal) {
        try {
            if (principal == null || principal.getName() == null || !hasText(notification.getConversationId())) {
                return;
            }
            String userId = principal.getName();
            if (!isParticipant(notification.getConversationId(), userId)) {
                return;
            }
            notification.setSender(userId);
            notification.setType(ChatNotificationType.LEAVE);
            messagingTemplate.convertAndSend("/topic/conversation/" + notification.getConversationId(), notification);
        } catch (Exception exception) {
            log.error("Failed to handle WebSocket user leave", exception);
        }
    }

    private boolean isParticipant(String conversationId, String userId) {
        return conversationRepository.findById(conversationId)
                .map(conversation -> conversation.getParticipants().stream()
                        .anyMatch(participant -> participant.getUserId().equals(userId)))
                .orElse(false);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
