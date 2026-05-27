package com.friendify.app.auth.mapper;

import com.friendify.app.auth.dto.request.PermissionRequest;
import com.friendify.app.auth.dto.response.PermissionResponse;
import com.friendify.app.auth.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
