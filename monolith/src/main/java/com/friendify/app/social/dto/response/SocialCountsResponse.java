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
public class SocialCountsResponse {
    long friendsCount;
    long followersCount;
    long followingCount;
    long pendingFriendRequestsCount;
    long sentFriendRequestsCount;
    long blockedUsersCount;
}
