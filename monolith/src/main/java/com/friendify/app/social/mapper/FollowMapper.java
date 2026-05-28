package com.friendify.app.social.mapper;

import com.friendify.app.social.dto.response.FollowResponse;
import com.friendify.app.social.entity.Follow;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FollowMapper {
    FollowResponse toFollowResponse(Follow follow);
}
