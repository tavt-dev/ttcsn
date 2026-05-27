package com.friendify.app.auth.mapper;

import com.friendify.app.auth.dto.request.RoleRequest;
import com.friendify.app.auth.dto.response.RoleResponse;
import com.friendify.app.auth.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
