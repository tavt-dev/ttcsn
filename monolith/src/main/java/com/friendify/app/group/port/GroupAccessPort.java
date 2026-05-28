package com.friendify.app.group.port;

import com.friendify.app.group.dto.response.GroupResponse;

public interface GroupAccessPort {
    boolean exists(String groupId);

    boolean canPost(String groupId, String userId);

    boolean canView(String groupId, String userId);

    GroupResponse getGroup(String groupId);
}
