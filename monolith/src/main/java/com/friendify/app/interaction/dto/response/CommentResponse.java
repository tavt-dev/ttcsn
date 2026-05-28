package com.friendify.app.interaction.dto.response;

import java.time.Instant;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    String id;
    String postId;
    String userId;
    String username;
    String userAvatar;
    String content;
    String parentCommentId;
    List<CommentResponse> replies;
    Integer replyCount;
    Integer likeCount;
    Boolean isLiked;
    Instant createdAt;
    Instant updatedAt;
}
