package com.friendify.app.group.dto.response;

import com.friendify.app.group.enums.RequestStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JoinRequestResponse {
    String id;
    String groupId;
    String groupName;
    String userId;
    String username;
    String avatar;
    RequestStatus status;
    String message;
    String requestedDate;
    String reviewedDate;
    String reviewedBy;
}
