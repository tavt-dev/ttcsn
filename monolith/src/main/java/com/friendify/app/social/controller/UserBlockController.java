package com.friendify.app.social.controller;

import com.friendify.app.shared.dto.ApiResponse;
import com.friendify.app.shared.dto.PageResponse;
import com.friendify.app.shared.security.CurrentUserProvider;
import com.friendify.app.social.dto.response.UserBlockResponse;
import com.friendify.app.social.service.UserBlockService;
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
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/social/blocks")
public class UserBlockController {
    UserBlockService userBlockService;
    CurrentUserProvider currentUserProvider;

    @PostMapping("/{blockedId}")
    public ApiResponse<UserBlockResponse> blockUser(@PathVariable String blockedId) {
        return ApiResponse.<UserBlockResponse>builder()
                .result(userBlockService.blockUser(currentUserProvider.getCurrentUserId(), blockedId))
                .build();
    }

    @DeleteMapping("/{blockedId}")
    public ApiResponse<Void> unblockUser(@PathVariable String blockedId) {
        userBlockService.unblockUser(currentUserProvider.getCurrentUserId(), blockedId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping
    public ApiResponse<PageResponse<UserBlockResponse>> getBlockedUsers(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<UserBlockResponse>>builder()
                .result(userBlockService.getBlockedUsers(currentUserProvider.getCurrentUserId(), page, size))
                .build();
    }

    @GetMapping("/check/{blockedId}")
    public ApiResponse<Boolean> checkBlocked(@PathVariable String blockedId) {
        return ApiResponse.<Boolean>builder()
                .result(userBlockService.checkBlocked(currentUserProvider.getCurrentUserId(), blockedId))
                .build();
    }
}
