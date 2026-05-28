package com.friendify.app.group.mapper;

import com.friendify.app.group.dto.response.GroupMemberResponse;
import com.friendify.app.group.entity.GroupMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GroupMemberMapper {
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "joinedDate", ignore = true)
    GroupMemberResponse toGroupMemberResponse(GroupMember groupMember);
}
