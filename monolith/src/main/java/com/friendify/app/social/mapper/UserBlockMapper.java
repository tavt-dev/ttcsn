package com.friendify.app.social.mapper;

import com.friendify.app.social.dto.response.UserBlockResponse;
import com.friendify.app.social.entity.UserBlock;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserBlockMapper {
    UserBlockResponse toUserBlockResponse(UserBlock userBlock);
}
