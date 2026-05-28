package com.friendify.app.interaction.port;

public interface InteractionQueryPort {
    long countLikesByPostId(String postId);

    long countCommentsByPostId(String postId);

    boolean isLikedByCurrentUser(String postId, String userId);
}
