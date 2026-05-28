package com.friendify.app.social.dto.response;

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
public class UserSocialInfoResponse {
    String userId;
    long followersCount;
    long followingCount;
    long friendsCount;
    boolean isFollowing;
    boolean isFriend;
    boolean isBlocked;
}
