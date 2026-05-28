package com.friendify.app.group.dto.response;

import com.friendify.app.group.enums.GroupPrivacy;
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
public class GroupResponse {
    String id;
    String name;
    String description;
    String coverImageUrl;
    String avatarUrl;
    String ownerId;
    String ownerName;
    String ownerAvatar;
    GroupPrivacy privacy;
    boolean requiresApproval;
    boolean allowPosting;
    boolean moderationRequired;
    boolean onlyAdminCanPost;
    long memberCount;
    long pendingRequestCount;
    String createdDate;
    String modifiedDate;
    boolean member;
    MemberRoleResponse memberRole;
}
