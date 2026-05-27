package com.friendify.app.notification.email;

import com.friendify.app.auth.port.NotificationDeliveryPort;
import com.friendify.app.notification.email.dto.Recipient;
import com.friendify.app.notification.email.dto.SendEmailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthEmailNotificationAdapter implements NotificationDeliveryPort {

    private final EmailDeliveryService emailDeliveryService;

    @Override
    public void sendEmail(String recipient, String subject, String htmlContent) {
        emailDeliveryService.sendEmail(new SendEmailRequest(new Recipient(null, recipient), subject, htmlContent));
    }
}
