package com.friendify.app.notification.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.friendify.app.notification.email.dto.EmailRequest;
import com.friendify.app.notification.email.dto.EmailResponse;
import com.friendify.app.notification.email.dto.Recipient;
import com.friendify.app.notification.email.dto.SendEmailRequest;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EmailDeliveryServiceTests {

    @Test
    void sendEmailFailsClearlyWhenBrevoApiKeyIsMissing() {
        BrevoEmailClient brevoEmailClient = mock(BrevoEmailClient.class);
        EmailDeliveryService emailDeliveryService =
                new EmailDeliveryService(brevoEmailClient, "", "Friendify", "sender@example.com");

        assertThatThrownBy(() -> emailDeliveryService.sendEmail(validRequest()))
                .isInstanceOf(AppException.class)
                .extracting(exception -> ((AppException) exception).getErrorCode())
                .isEqualTo(ErrorCode.NOTIFICATION_DELIVERY_NOT_CONFIGURED);
        verify(brevoEmailClient, never()).sendEmail(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void sendEmailFailsClearlyWhenSenderEmailIsMissing() {
        BrevoEmailClient brevoEmailClient = mock(BrevoEmailClient.class);
        EmailDeliveryService emailDeliveryService =
                new EmailDeliveryService(brevoEmailClient, "api-key", "Friendify", "");

        assertThatThrownBy(() -> emailDeliveryService.sendEmail(validRequest()))
                .isInstanceOf(AppException.class)
                .extracting(exception -> ((AppException) exception).getErrorCode())
                .isEqualTo(ErrorCode.NOTIFICATION_DELIVERY_NOT_CONFIGURED);
        verify(brevoEmailClient, never()).sendEmail(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void sendEmailBuildsBrevoRequestAndCallsClient() {
        BrevoEmailClient brevoEmailClient = mock(BrevoEmailClient.class);
        EmailDeliveryService emailDeliveryService =
                new EmailDeliveryService(brevoEmailClient, "api-key", "Friendify", "sender@example.com");
        EmailResponse response = new EmailResponse("message-1");
        when(brevoEmailClient.sendEmail(org.mockito.ArgumentMatchers.eq("api-key"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        EmailResponse result = emailDeliveryService.sendEmail(validRequest());

        ArgumentCaptor<EmailRequest> requestCaptor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(brevoEmailClient).sendEmail(org.mockito.ArgumentMatchers.eq("api-key"), requestCaptor.capture());
        EmailRequest emailRequest = requestCaptor.getValue();
        assertThat(result).isSameAs(response);
        assertThat(emailRequest.sender().name()).isEqualTo("Friendify");
        assertThat(emailRequest.sender().email()).isEqualTo("sender@example.com");
        assertThat(emailRequest.to()).containsExactly(new Recipient(null, "alice@example.com"));
        assertThat(emailRequest.subject()).isEqualTo("Verify email");
        assertThat(emailRequest.htmlContent()).contains("123456");
    }

    private SendEmailRequest validRequest() {
        return new SendEmailRequest(
                new Recipient(null, "alice@example.com"),
                "Verify email",
                "<p>123456</p>");
    }
}
