package com.friendify.app.notification.controller;

import com.friendify.app.notification.dto.response.NotificationResponse;
import com.friendify.app.notification.service.NotificationService;
import com.friendify.app.shared.dto.ApiResponse;
import com.friendify.app.shared.dto.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserNotificationController {
    NotificationService notificationService;

    @GetMapping
    ApiResponse<PageResponse<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<PageResponse<NotificationResponse>>builder()
                .result(notificationService.getMyNotifications(page, size))
                .build();
    }

    @PutMapping("/{id}/read")
    ApiResponse<NotificationResponse> markAsRead(@PathVariable String id) {
        return ApiResponse.<NotificationResponse>builder()
                .result(notificationService.markAsRead(id))
                .build();
    }

    @PutMapping("/read-all")
    ApiResponse<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ApiResponse.<Void>builder()
                .message("All notifications were marked as read")
                .build();
    }

    @GetMapping("/unread-count")
    ApiResponse<Long> getUnreadCount() {
        return ApiResponse.<Long>builder()
                .result(notificationService.getUnreadCount())
                .build();
    }
}
