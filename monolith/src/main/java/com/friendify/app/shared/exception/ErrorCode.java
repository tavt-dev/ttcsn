package com.friendify.app.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid error key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1101, "User already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User does not exist", HttpStatus.NOT_FOUND),
    USER_DISABLED(1103, "User account is disabled", HttpStatus.FORBIDDEN),
    USER_ALREADY_VERIFIED(1104, "User is already verified", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1201, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1202, "Unauthorized", HttpStatus.FORBIDDEN),
    INVALID_PASSWORD(1203, "Password must have at least {min} characters", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1204, "Username must have at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_OLD_PASSWORD(1205, "Old password is incorrect", HttpStatus.BAD_REQUEST),
    WRONG_PASSWORD(1206, "Password is incorrect", HttpStatus.BAD_REQUEST),
    INVALID_DOB(1301, "Invalid date of birth", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1302, "Invalid email address", HttpStatus.BAD_REQUEST),
    EMAIL_IS_REQUIRED(1303, "Email is required", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED(1300, "Validation failed", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1304, "Email already exists", HttpStatus.BAD_REQUEST),
    OTP_NOT_FOUND(1401, "OTP was not found", HttpStatus.NOT_FOUND),
    OTP_EXPIRED(1402, "OTP has expired", HttpStatus.BAD_REQUEST),
    OTP_INVALID(1403, "OTP is invalid", HttpStatus.BAD_REQUEST),
    OTP_TOO_FREQUENT(1404, "Please wait before requesting another OTP", HttpStatus.TOO_MANY_REQUESTS),
    NOTIFICATION_DELIVERY_NOT_CONFIGURED(1501, "Notification delivery is not configured", HttpStatus.SERVICE_UNAVAILABLE),
    NOTIFICATION_DELIVERY_FAILED(1502, "Notification delivery failed", HttpStatus.BAD_GATEWAY);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
