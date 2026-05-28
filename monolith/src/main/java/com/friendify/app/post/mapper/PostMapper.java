package com.friendify.app.post.mapper;

import com.friendify.app.post.dto.response.PostResponse;
import com.friendify.app.post.entity.Post;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostResponse toPostResponse(Post post);
}
