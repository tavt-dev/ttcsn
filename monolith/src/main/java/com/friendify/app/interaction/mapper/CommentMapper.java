package com.friendify.app.interaction.mapper;

import com.friendify.app.interaction.dto.response.CommentResponse;
import com.friendify.app.interaction.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "userAvatar", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "replyCount", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "isLiked", ignore = true)
    CommentResponse toCommentResponse(Comment comment);
}
