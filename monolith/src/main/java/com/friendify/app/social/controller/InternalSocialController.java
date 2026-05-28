package com.friendify.app.social.controller;

import java.util.List;

import com.friendify.app.shared.dto.ApiResponse;
import com.friendify.app.shared.security.CurrentUserProvider;
import com.friendify.app.social.port.SocialGraphQueryPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/internal")
public class InternalSocialController {
    SocialGraphQueryPort socialGraphQueryPort;
    CurrentUserProvider currentUserProvider;

    @GetMapping("/friend-ids")
    public ApiResponse<List<String>> getFriendIds() {
        return ApiResponse.<List<String>>builder()
                .result(socialGraphQueryPort.getFriendIds(currentUserProvider.getCurrentUserId()))
                .build();
    }

    @GetMapping("/following-ids")
    public ApiResponse<List<String>> getFollowingIds() {
        return ApiResponse.<List<String>>builder()
                .result(socialGraphQueryPort.getFollowingIds(currentUserProvider.getCurrentUserId()))
                .build();
    }

    @GetMapping("/blocks/ids")
    public ApiResponse<List<String>> getBlockedUserIds() {
        return ApiResponse.<List<String>>builder()
                .result(socialGraphQueryPort.getBlockedUserIds(currentUserProvider.getCurrentUserId()))
                .build();
    }
}
