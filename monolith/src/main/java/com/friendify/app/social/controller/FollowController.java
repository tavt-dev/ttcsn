package com.friendify.app.social.controller;

import com.friendify.app.shared.dto.ApiResponse;
import com.friendify.app.shared.dto.PageResponse;
import com.friendify.app.shared.security.CurrentUserProvider;
import com.friendify.app.social.dto.response.FollowResponse;
import com.friendify.app.social.dto.response.UserSocialInfoResponse;
import com.friendify.app.social.service.FollowService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/social/follows")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FollowController {
    FollowService followService;
    CurrentUserProvider currentUserProvider;

    @PostMapping("/{followingId}")
    public ApiResponse<FollowResponse> followUser(@PathVariable String followingId) {
        return ApiResponse.<FollowResponse>builder()
                .result(followService.followUser(currentUserProvider.getCurrentUserId(), followingId))
                .build();
    }

    @DeleteMapping("/{followingId}")
    public ApiResponse<Void> unfollowUser(@PathVariable String followingId) {
        followService.unfollowUser(currentUserProvider.getCurrentUserId(), followingId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/following/{userId}")
    public ApiResponse<PageResponse<FollowResponse>> getFollowingUser(
            @PathVariable String userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        return ApiResponse.<PageResponse<FollowResponse>>builder()
                .result(followService.getFollowingUser(userId, page, size))
                .build();
    }

    @GetMapping("/followers/{userId}")
    public ApiResponse<PageResponse<FollowResponse>> getFollowerUser(
            @PathVariable String userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        return ApiResponse.<PageResponse<FollowResponse>>builder()
                .result(followService.getFollowerUser(userId, page, size))
                .build();
    }

    @GetMapping("/info/{userId}")
    public ApiResponse<UserSocialInfoResponse> getUserSocialInfo(@PathVariable String userId) {
        return ApiResponse.<UserSocialInfoResponse>builder()
                .result(followService.getUserSocialInfo(currentUserProvider.getCurrentUserId(), userId))
                .build();
    }
}
