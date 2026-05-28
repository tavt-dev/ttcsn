package com.friendify.app.interaction.controller;

import com.friendify.app.interaction.dto.request.CreateCommentRequest;
import com.friendify.app.interaction.dto.request.UpdateCommentRequest;
import com.friendify.app.interaction.dto.response.CommentResponse;
import com.friendify.app.interaction.service.CommentService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interaction/comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {
    CommentService commentService;

    @PostMapping
    ApiResponse<CommentResponse> createComment(@Valid @RequestBody CreateCommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .message("Create comment successfully")
                .result(commentService.createComment(request))
                .build();
    }

    @GetMapping("/post/{postId}")
    ApiResponse<PageResponse<CommentResponse>> getCommentsByPost(
            @PathVariable String postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<CommentResponse>>builder()
                .result(commentService.getCommentsByPost(postId, page, size))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<CommentResponse> getCommentById(@PathVariable String id) {
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.getCommentById(id))
                .build();
    }

    @GetMapping("/{id}/replies")
    ApiResponse<PageResponse<CommentResponse>> getReplies(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<CommentResponse>>builder()
                .result(commentService.getRepliesByCommentId(id, page, size))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<CommentResponse> updateComment(
            @PathVariable String id,
            @Valid @RequestBody UpdateCommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.updateComment(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteComment(@PathVariable String id) {
        commentService.deleteComment(id);
        return ApiResponse.<Void>builder()
                .message("Delete comment successfully")
                .build();
    }
}
