package com.friendify.app.social.port;

import java.util.List;

public interface SocialGraphQueryPort {
    List<String> getFriendIds(String currentUserId);

    List<String> getFollowingIds(String currentUserId);

    List<String> getBlockedUserIds(String currentUserId);

    boolean isBlockedBetween(String userId1, String userId2);
}
