package com.friendify.app.notification.email;

import com.friendify.app.notification.email.dto.EmailRequest;
import com.friendify.app.notification.email.dto.EmailResponse;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RestClientBrevoEmailClient implements BrevoEmailClient {

    private final RestClient restClient;
    private final String brevoUrl;

    public RestClientBrevoEmailClient(
            RestClient.Builder restClientBuilder,
            @Value("${notification.email.brevo-url:https://api.brevo.com/v3/smtp/email}") String brevoUrl) {
        this.restClient = restClientBuilder.build();
        this.brevoUrl = brevoUrl;
    }

    @Override
    public EmailResponse sendEmail(String apiKey, EmailRequest request) {
        try {
            return restClient.post()
                    .uri(brevoUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("api-key", apiKey)
                    .body(request)
                    .retrieve()
                    .body(EmailResponse.class);
        } catch (RestClientException exception) {
            throw new AppException(ErrorCode.NOTIFICATION_DELIVERY_FAILED);
        }
    }
}
