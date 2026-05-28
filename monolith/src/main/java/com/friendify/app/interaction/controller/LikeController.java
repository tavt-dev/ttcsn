package com.friendify.app.interaction.controller;

import com.friendify.app.interaction.dto.request.CreateLikeRequest;
import com.friendify.app.interaction.dto.response.LikeResponse;
import com.friendify.app.interaction.service.LikeService;
import com.friendify.app.shared.dto.ApiResponse;
import com.friendify.app.shared.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interaction/likes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LikeController {
    LikeService likeService;

    @PostMapping
    ApiResponse<LikeResponse> createLike(@Valid @RequestBody CreateLikeRequest request) {
        return ApiResponse.<LikeResponse>builder()
                .message("Like successfully")
                .result(likeService.createLike(request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> unlike(@PathVariable String id) {
        likeService.unlike(id);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/post/{postId}")
    ApiResponse<Void> unlikeByPost(@PathVariable String postId) {
        likeService.unlikeByPost(postId);
        return ApiResponse.<Void>builder()
                .message("Unlike successfully")
                .build();
    }

    @DeleteMapping("/comment/{commentId}")
    ApiResponse<Void> unlikeByComment(@PathVariable String commentId) {
        likeService.unlikeByComment(commentId);
        return ApiResponse.<Void>builder()
                .message("Unlike successfully")
                .build();
    }

    @GetMapping("/post/{postId}")
    ApiResponse<PageResponse<LikeResponse>> getLikesByPost(
            @PathVariable String postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<LikeResponse>>builder()
                .result(likeService.getLikesByPost(postId, page, size))
                .build();
    }
}
