package com.friendify.app.notification.email;

import java.util.List;

import com.friendify.app.notification.email.dto.EmailRequest;
import com.friendify.app.notification.email.dto.EmailResponse;
import com.friendify.app.notification.email.dto.SendEmailRequest;
import com.friendify.app.notification.email.dto.Sender;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailDeliveryService {

    private final BrevoEmailClient brevoEmailClient;
    private final String apiKey;
    private final String senderName;
    private final String senderEmail;

    public EmailDeliveryService(
            BrevoEmailClient brevoEmailClient,
            @Value("${notification.email.brevo-apikey:}") String apiKey,
            @Value("${notification.email.sender-name:Friendify}") String senderName,
            @Value("${notification.email.sender-email:}") String senderEmail) {
        this.brevoEmailClient = brevoEmailClient;
        this.apiKey = apiKey;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
    }

    public EmailResponse sendEmail(SendEmailRequest request) {
        validateRequest(request);
        if (!StringUtils.hasText(apiKey)) {
            throw new AppException(ErrorCode.NOTIFICATION_DELIVERY_NOT_CONFIGURED);
        }
        if (!StringUtils.hasText(senderEmail)) {
            throw new AppException(ErrorCode.NOTIFICATION_DELIVERY_NOT_CONFIGURED);
        }

        EmailRequest emailRequest = new EmailRequest(
                new Sender(senderName, senderEmail),
                List.of(request.to()),
                request.htmlContent(),
                request.subject());

        try {
            return brevoEmailClient.sendEmail(apiKey, emailRequest);
        } catch (AppException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new AppException(ErrorCode.NOTIFICATION_DELIVERY_FAILED);
        }
    }

    private void validateRequest(SendEmailRequest request) {
        if (request == null
                || request.to() == null
                || !StringUtils.hasText(request.to().email())
                || !StringUtils.hasText(request.subject())
                || !StringUtils.hasText(request.htmlContent())) {
            throw new AppException(ErrorCode.NOTIFICATION_DELIVERY_FAILED);
        }
    }
}
