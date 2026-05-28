package com.friendify.app.social.dto.response;

import java.time.LocalDateTime;

import com.friendify.app.social.enums.FriendshipStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendshipResponse {
    String id;
    String userId;
    String friendId;
    FriendshipStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
