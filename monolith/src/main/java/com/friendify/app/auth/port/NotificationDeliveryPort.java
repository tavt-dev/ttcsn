package com.friendify.app.auth.port;

public interface NotificationDeliveryPort {
    void sendEmail(String recipient, String subject, String htmlContent);
}
