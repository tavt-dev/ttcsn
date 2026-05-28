package com.friendify.app.social.mapper;

import com.friendify.app.social.dto.response.FriendshipResponse;
import com.friendify.app.social.entity.Friendship;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FriendshipMapper {
    FriendshipResponse toFriendshipResponse(Friendship friendship);
}
