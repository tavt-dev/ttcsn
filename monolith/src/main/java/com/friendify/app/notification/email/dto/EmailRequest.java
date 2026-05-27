package com.friendify.app.notification.email.dto;

import java.util.List;

public record EmailRequest(
        Sender sender,
        List<Recipient> to,
        String htmlContent,
        String subject) {
}
