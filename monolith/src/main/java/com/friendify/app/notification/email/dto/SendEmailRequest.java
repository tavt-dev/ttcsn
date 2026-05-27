package com.friendify.app.notification.email.dto;

public record SendEmailRequest(
        Recipient to,
        String subject,
        String htmlContent) {
}
