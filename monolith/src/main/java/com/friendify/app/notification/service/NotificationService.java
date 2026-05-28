package com.friendify.app.notification.service;

import java.time.Instant;
import java.util.Map;

import com.friendify.app.notification.dto.response.NotificationResponse;
import com.friendify.app.notification.email.EmailDeliveryService;
import com.friendify.app.notification.email.dto.Recipient;
import com.friendify.app.notification.email.dto.SendEmailRequest;
import com.friendify.app.notification.entity.Notification;
import com.friendify.app.notification.mapper.NotificationMapper;
import com.friendify.app.notification.port.NotificationCreatePort;
import com.friendify.app.notification.repository.NotificationRepository;
import com.friendify.app.shared.dto.PageResponse;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.friendify.app.shared.notification.NotificationEvent;
import com.friendify.app.shared.security.CurrentUserProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService implements NotificationCreatePort {
    NotificationRepository notificationRepository;
    NotificationMapper notificationMapper;
    CurrentUserProvider currentUserProvider;
    EmailDeliveryService emailDeliveryService;

    public PageResponse<NotificationResponse> getMyNotifications(int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Notification> notificationsPage =
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        var content = notificationsPage.getContent().stream()
                .map(notificationMapper::toNotificationResponse)
                .toList();

        return PageResponse.<NotificationResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(notificationsPage.getTotalElements())
                .totalPages(notificationsPage.getTotalPages())
                .hasNext(notificationsPage.hasNext())
                .hasPrevious(notificationsPage.hasPrevious())
                .build();
    }

    @Transactional
    public NotificationResponse markAsRead(String notificationId) {
        String userId = currentUserProvider.getCurrentUserId();
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
        }

        return notificationMapper.toNotificationResponse(notification);
    }

    @Transactional
    public void markAllAsRead() {
        String userId = currentUserProvider.getCurrentUserId();
        var notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged());
        var unreadNotifications = notifications.getContent().stream()
                .filter(notification -> !Boolean.TRUE.equals(notification.getIsRead()))
                .toList();

        if (!unreadNotifications.isEmpty()) {
            Instant now = Instant.now();
            unreadNotifications.forEach(notification -> {
                notification.setIsRead(true);
                notification.setReadAt(now);
            });
            notificationRepository.saveAll(unreadNotifications);
        }
    }

    public Long getUnreadCount() {
        return notificationRepository.countByUserIdAndIsReadFalse(currentUserProvider.getCurrentUserId());
    }

    @Override
    public NotificationResponse createNotification(
            String userId,
            String type,
            String title,
            String content,
            String relatedUserId,
            String relatedEntityId,
            String relatedEntityType) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .relatedUserId(relatedUserId)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .isRead(false)
                .createdAt(Instant.now())
                .build();

        notification = notificationRepository.save(notification);
        log.info("Created notification {} for user {}", notification.getId(), userId);
        return notificationMapper.toNotificationResponse(notification);
    }

    @Override
    public NotificationResponse createNotificationFromEvent(NotificationEvent event) {
        if (event == null || event.getParam() == null) {
            throw new AppException(ErrorCode.NOTIFICATION_DELIVERY_FAILED);
        }

        Map<String, Object> param = event.getParam();
        Object userId = param.get("userId");
        if (userId == null) {
            throw new AppException(ErrorCode.NOTIFICATION_DELIVERY_FAILED);
        }

        return createNotification(
                userId.toString(),
                value(param, "type", "SYSTEM"),
                event.getSubject() == null ? "Notification" : event.getSubject(),
                event.getBody() == null ? "" : event.getBody(),
                value(param, "relatedUserId", null),
                value(param, "relatedEntityId", null),
                value(param, "relatedEntityType", null));
    }

    @Transactional
    public NotificationResponse deliver(NotificationEvent event) {
        if (event == null) {
            throw new AppException(ErrorCode.NOTIFICATION_DELIVERY_FAILED);
        }

        emailDeliveryService.sendEmail(new SendEmailRequest(
                new Recipient(null, event.getRecipient()),
                event.getSubject(),
                event.getBody()));

        if (event.getParam() != null && event.getParam().get("userId") != null) {
            return createNotificationFromEvent(event);
        }

        return null;
    }

    private String value(Map<String, Object> param, String key, String defaultValue) {
        Object value = param.get(key);
        return value == null ? defaultValue : value.toString();
    }
}
