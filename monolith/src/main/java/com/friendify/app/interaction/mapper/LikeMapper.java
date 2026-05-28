package com.friendify.app.interaction.mapper;

import com.friendify.app.interaction.dto.response.LikeResponse;
import com.friendify.app.interaction.entity.Like;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LikeMapper {
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "userAvatar", ignore = true)
    LikeResponse toLikeResponse(Like like);
}
