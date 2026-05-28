package com.friendify.app.group.dto.request;

import com.friendify.app.group.enums.MemberRole;
import jakarta.validation.constraints.NotNull;
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
public class UpdateMemberRoleRequest {
    @NotNull(message = "Role is required")
    MemberRole role;
}
