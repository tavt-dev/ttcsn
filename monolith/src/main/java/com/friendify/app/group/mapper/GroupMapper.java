package com.friendify.app.group.mapper;

import com.friendify.app.group.dto.response.GroupResponse;
import com.friendify.app.group.entity.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    @Mapping(target = "ownerName", ignore = true)
    @Mapping(target = "ownerAvatar", ignore = true)
    @Mapping(target = "memberCount", ignore = true)
    @Mapping(target = "pendingRequestCount", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "memberRole", ignore = true)
    GroupResponse toGroupResponse(Group group);
}
