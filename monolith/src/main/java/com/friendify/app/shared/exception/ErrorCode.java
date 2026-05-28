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
    NOTIFICATION_DELIVERY_FAILED(1502, "Notification delivery failed", HttpStatus.BAD_GATEWAY),
    FILE_NOT_FOUND(1601, "File does not exist", HttpStatus.NOT_FOUND),
    FILE_EMPTY(1602, "File is empty", HttpStatus.BAD_REQUEST),
    POST_ID_REQUIRED(1603, "Post id is required", HttpStatus.BAD_REQUEST),
    OWNER_ID_REQUIRED(1604, "Owner id is required", HttpStatus.BAD_REQUEST),
    CLOUDINARY_UPLOAD_FAILED(1605, "Cloudinary upload failed", HttpStatus.BAD_GATEWAY),
    FILE_TYPE_NOT_ALLOWED(1606, "File type is not allowed", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(1607, "File size exceeds the allowed limit", HttpStatus.BAD_REQUEST),
    INVALID_KEYWORD(1701, "Keyword is invalid", HttpStatus.BAD_REQUEST),
    FOLLOW_ALREADY_EXISTS(1702, "Follow relationship already exists", HttpStatus.BAD_REQUEST),
    FOLLOW_NOT_FOUND(1703, "Follow relationship was not found", HttpStatus.NOT_FOUND),
    CANNOT_FOLLOW_SELF(1704, "Cannot follow yourself", HttpStatus.BAD_REQUEST),
    FRIENDSHIP_ALREADY_EXISTS(1710, "Friendship already exists", HttpStatus.BAD_REQUEST),
    FRIENDSHIP_NOT_FOUND(1711, "Friendship was not found", HttpStatus.NOT_FOUND),
    CANNOT_FRIEND_SELF(1712, "Cannot send a friend request to yourself", HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_ALREADY_SENT(1713, "Friend request was already sent", HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_NOT_PENDING(1714, "Friend request is not pending", HttpStatus.BAD_REQUEST),
    USER_ALREADY_BLOCKED(1720, "User is already blocked", HttpStatus.BAD_REQUEST),
    USER_NOT_BLOCKED(1721, "User is not blocked", HttpStatus.NOT_FOUND),
    CANNOT_BLOCK_SELF(1722, "Cannot block yourself", HttpStatus.BAD_REQUEST),
    GROUP_NOT_FOUND(1801, "Group was not found", HttpStatus.NOT_FOUND),
    GROUP_ALREADY_EXISTS(1802, "Group already exists", HttpStatus.BAD_REQUEST),
    GROUP_NOT_OWNER(1803, "You are not the owner of this group", HttpStatus.FORBIDDEN),
    GROUP_NAME_REQUIRED(1804, "Group name is required", HttpStatus.BAD_REQUEST),
    MEMBER_NOT_FOUND(1805, "Group member was not found", HttpStatus.NOT_FOUND),
    MEMBER_ALREADY_EXISTS(1806, "Group member already exists", HttpStatus.BAD_REQUEST),
    MEMBER_CANNOT_REMOVE_OWNER(1807, "Group owner cannot be removed", HttpStatus.BAD_REQUEST),
    INVALID_ROLE(1808, "Member role is invalid", HttpStatus.BAD_REQUEST),
    CANNOT_CHANGE_OWNER_ROLE(1809, "Cannot change owner role", HttpStatus.BAD_REQUEST),
    JOIN_REQUEST_NOT_FOUND(1810, "Join request was not found", HttpStatus.NOT_FOUND),
    JOIN_REQUEST_ALREADY_EXISTS(1811, "Join request already exists", HttpStatus.BAD_REQUEST),
    ALREADY_MEMBER(1812, "User is already a group member", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_PERMISSION(1813, "Insufficient permission", HttpStatus.FORBIDDEN),
    CANNOT_JOIN_GROUP(1814, "Cannot join this group", HttpStatus.BAD_REQUEST),
    POSTING_NOT_ALLOWED(1815, "Posting is not allowed in this group", HttpStatus.FORBIDDEN),
    POST_NOT_FOUND(1901, "Post was not found", HttpStatus.NOT_FOUND),
    POST_MODULE_NOT_MIGRATED(1902, "Post module is not migrated into the monolith yet", HttpStatus.SERVICE_UNAVAILABLE),
    COMMENT_NOT_FOUND(1910, "Comment was not found", HttpStatus.NOT_FOUND),
    INVALID_PARENT_COMMENT(1911, "Parent comment is invalid", HttpStatus.BAD_REQUEST),
    LIKE_NOT_FOUND(1920, "Like was not found", HttpStatus.NOT_FOUND),
    ALREADY_LIKED(1921, "Already liked", HttpStatus.BAD_REQUEST),
    INVALID_LIKE_REQUEST(1922, "Like request is invalid", HttpStatus.BAD_REQUEST);

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
