package com.friendify.app.post.controller;

import com.friendify.app.post.service.PostService;
import com.friendify.app.shared.dto.ApiResponse;
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
public class InternalPostController {
    PostService postService;

    @GetMapping("/posts/{postId}/exists")
    ApiResponse<Boolean> checkPostExists(@PathVariable String postId) {
        return ApiResponse.<Boolean>builder()
                .code(200)
                .message("Check post exists")
                .result(postService.checkPostExists(postId))
                .build();
    }
}
