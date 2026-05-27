package com.friendify.app.auth.mapper;

import com.friendify.app.auth.dto.request.UserCreationRequest;
import com.friendify.app.auth.dto.request.UserUpdateRequest;
import com.friendify.app.auth.dto.response.UserResponse;
import com.friendify.app.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
