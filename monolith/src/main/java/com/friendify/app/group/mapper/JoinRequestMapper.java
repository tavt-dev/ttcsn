package com.friendify.app.group.mapper;

import com.friendify.app.group.dto.response.JoinRequestResponse;
import com.friendify.app.group.entity.JoinRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JoinRequestMapper {
    @Mapping(target = "groupName", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "requestedDate", ignore = true)
    @Mapping(target = "reviewedDate", ignore = true)
    JoinRequestResponse toJoinRequestResponse(JoinRequest joinRequest);
}
