package com.friendify.app.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.friendify.app.notification.dto.response.NotificationResponse;
import com.friendify.app.notification.email.EmailDeliveryService;
import com.friendify.app.notification.email.dto.SendEmailRequest;
import com.friendify.app.notification.entity.Notification;
import com.friendify.app.notification.mapper.NotificationMapper;
import com.friendify.app.notification.port.NotificationCreatePort;
import com.friendify.app.notification.repository.NotificationRepository;
import com.friendify.app.shared.notification.NotificationEvent;
import com.friendify.app.shared.security.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTests {

    @Mock
    NotificationRepository notificationRepository;

    @Mock
    NotificationMapper notificationMapper;

    @Mock
    CurrentUserProvider currentUserProvider;

    @Mock
    EmailDeliveryService emailDeliveryService;

    @InjectMocks
    NotificationService notificationService;

    @Test
    void implementsNotificationCreatePort() {
        assertThat(notificationService).isInstanceOf(NotificationCreatePort.class);
    }

    @Test
    void getMyNotificationsUsesCurrentUser() {
        Notification notification = Notification.builder()
                .id("notification-1")
                .userId("user-1")
                .title("Title")
                .createdAt(Instant.now())
                .isRead(false)
                .build();
        NotificationResponse response = NotificationResponse.builder()
                .id("notification-1")
                .userId("user-1")
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn("user-1");
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(
                        org.mockito.ArgumentMatchers.eq("user-1"), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification)));
        when(notificationMapper.toNotificationResponse(notification)).thenReturn(response);

        var result = notificationService.getMyNotifications(1, 20);

        assertThat(result.getContent()).containsExactly(response);
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq("user-1"), org.mockito.ArgumentMatchers.any(Pageable.class));
    }

    @Test
    void markAsReadSetsReadFields() {
        Notification notification = Notification.builder()
                .id("notification-1")
                .userId("user-1")
                .isRead(false)
                .build();
        Notification saved = Notification.builder()
                .id("notification-1")
                .userId("user-1")
                .isRead(true)
                .readAt(Instant.now())
                .build();
        NotificationResponse response = NotificationResponse.builder()
                .id("notification-1")
                .isRead(true)
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn("user-1");
        when(notificationRepository.findByIdAndUserId("notification-1", "user-1")).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(saved);
        when(notificationMapper.toNotificationResponse(saved)).thenReturn(response);

        NotificationResponse result = notificationService.markAsRead("notification-1");

        assertThat(result.getIsRead()).isTrue();
        assertThat(notification.getIsRead()).isTrue();
        assertThat(notification.getReadAt()).isNotNull();
    }

    @Test
    void createNotificationFromEventUsesEventParams() {
        NotificationEvent event = NotificationEvent.builder()
                .subject("Friend request")
                .body("Alice sent a friend request")
                .param(Map.of(
                        "userId", "user-1",
                        "type", "FRIEND_REQUEST",
                        "relatedUserId", "user-2",
                        "relatedEntityId", "request-1",
                        "relatedEntityType", "FRIENDSHIP"))
                .build();
        Notification saved = Notification.builder()
                .id("notification-1")
                .userId("user-1")
                .type("FRIEND_REQUEST")
                .title("Friend request")
                .content("Alice sent a friend request")
                .build();
        NotificationResponse response = NotificationResponse.builder()
                .id("notification-1")
                .userId("user-1")
                .type("FRIEND_REQUEST")
                .build();

        when(notificationRepository.save(org.mockito.ArgumentMatchers.any(Notification.class))).thenReturn(saved);
        when(notificationMapper.toNotificationResponse(saved)).thenReturn(response);

        NotificationResponse result = notificationService.createNotificationFromEvent(event);

        assertThat(result.getType()).isEqualTo("FRIEND_REQUEST");
        verify(notificationRepository).save(org.mockito.ArgumentMatchers.argThat(notification ->
                "user-1".equals(notification.getUserId())
                        && "FRIEND_REQUEST".equals(notification.getType())
                        && "request-1".equals(notification.getRelatedEntityId())));
    }

    @Test
    void deliverSendsEmailAndCreatesNotificationFromEvent() {
        NotificationEvent event = NotificationEvent.builder()
                .recipient("alice@example.com")
                .subject("Friend request")
                .body("Alice sent a friend request")
                .param(Map.of("userId", "user-1", "type", "FRIEND_REQUEST"))
                .build();
        Notification saved = Notification.builder()
                .id("notification-1")
                .userId("user-1")
                .type("FRIEND_REQUEST")
                .build();
        NotificationResponse response = NotificationResponse.builder()
                .id("notification-1")
                .userId("user-1")
                .type("FRIEND_REQUEST")
                .build();

        when(notificationRepository.save(org.mockito.ArgumentMatchers.any(Notification.class))).thenReturn(saved);
        when(notificationMapper.toNotificationResponse(saved)).thenReturn(response);

        NotificationResponse result = notificationService.deliver(event);

        assertThat(result).isSameAs(response);
        verify(emailDeliveryService).sendEmail(org.mockito.ArgumentMatchers.argThat((SendEmailRequest request) ->
                "alice@example.com".equals(request.to().email())
                        && "Friend request".equals(request.subject())
                        && "Alice sent a friend request".equals(request.htmlContent())));
    }
}
