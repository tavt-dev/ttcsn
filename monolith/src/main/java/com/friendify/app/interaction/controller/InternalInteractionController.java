package com.friendify.app.interaction.controller;

import com.friendify.app.interaction.port.InteractionQueryPort;
import com.friendify.app.shared.dto.ApiResponse;
import com.friendify.app.shared.security.CurrentUserProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalInteractionController {
    InteractionQueryPort interactionQueryPort;
    CurrentUserProvider currentUserProvider;

    @GetMapping("/likes/post/{postId}/count")
    ApiResponse<Long> getLikeCountByPost(@PathVariable String postId) {
        return ApiResponse.<Long>builder()
                .result(interactionQueryPort.countLikesByPostId(postId))
                .build();
    }

    @GetMapping("/likes/post/{postId}/is-liked")
    ApiResponse<Boolean> isPostLiked(@PathVariable String postId) {
        return ApiResponse.<Boolean>builder()
                .result(interactionQueryPort.isLikedByCurrentUser(postId, currentUserProvider.getCurrentUserId()))
                .build();
    }

    @GetMapping("/comments/post/{postId}/count")
    ApiResponse<Long> getCommentCountByPost(@PathVariable String postId) {
        return ApiResponse.<Long>builder()
                .result(interactionQueryPort.countCommentsByPostId(postId))
                .build();
    }
}
