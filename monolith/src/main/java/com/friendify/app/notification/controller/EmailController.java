package com.friendify.app.notification.controller;

import com.friendify.app.notification.email.EmailDeliveryService;
import com.friendify.app.notification.email.dto.EmailResponse;
import com.friendify.app.notification.email.dto.SendEmailRequest;
import com.friendify.app.shared.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification/email")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailController {
    EmailDeliveryService emailDeliveryService;

    @PostMapping("/send")
    ApiResponse<EmailResponse> sendEmail(@RequestBody SendEmailRequest request) {
        return ApiResponse.<EmailResponse>builder()
                .result(emailDeliveryService.sendEmail(request))
                .build();
    }
}
