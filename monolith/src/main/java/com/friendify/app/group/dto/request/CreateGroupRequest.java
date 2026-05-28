package com.friendify.app.group.dto.request;

import com.friendify.app.group.enums.GroupPrivacy;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateGroupRequest {
    @NotBlank(message = "Group name is required")
    String name;
    String description;
    String coverImageUrl;
    String avatarUrl;
    GroupPrivacy privacy;
    Boolean requiresApproval;
    Boolean allowPosting;
    Boolean moderationRequired;
    Boolean onlyAdminCanPost;
}
