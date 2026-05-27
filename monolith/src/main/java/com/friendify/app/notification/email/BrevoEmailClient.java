package com.friendify.app.notification.email;

import com.friendify.app.notification.email.dto.EmailRequest;
import com.friendify.app.notification.email.dto.EmailResponse;

public interface BrevoEmailClient {
    EmailResponse sendEmail(String apiKey, EmailRequest request);
}
