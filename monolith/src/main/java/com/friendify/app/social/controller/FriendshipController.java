package com.friendify.app.social.controller;

import java.util.List;

import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.shared.dto.ApiResponse;
import com.friendify.app.shared.dto.PageResponse;
import com.friendify.app.shared.security.CurrentUserProvider;
import com.friendify.app.social.dto.response.FriendshipResponse;
import com.friendify.app.social.dto.response.FriendshipStatusResponse;
import com.friendify.app.social.dto.response.SocialCountsResponse;
import com.friendify.app.social.service.FriendshipService;
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
@RequestMapping("/api/v1/social/friendships")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendshipController {
    FriendshipService friendshipService;
    CurrentUserProvider currentUserProvider;

    @PostMapping("/{friendId}")
    public ApiResponse<FriendshipResponse> sendFriendRequest(@PathVariable String friendId) {
        return ApiResponse.<FriendshipResponse>builder()
                .result(friendshipService.sendFriendRequest(currentUserProvider.getCurrentUserId(), friendId))
                .build();
    }

    @PostMapping("/{friendId}/accept")
    public ApiResponse<FriendshipResponse> acceptFriendRequest(@PathVariable String friendId) {
        return ApiResponse.<FriendshipResponse>builder()
                .result(friendshipService.acceptFriendRequest(currentUserProvider.getCurrentUserId(), friendId))
                .build();
    }

    @PostMapping("/{friendId}/reject")
    public ApiResponse<Void> rejectFriendRequest(@PathVariable String friendId) {
        friendshipService.rejectFriendRequest(currentUserProvider.getCurrentUserId(), friendId);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/{friendId}")
    public ApiResponse<Void> removeFriend(@PathVariable String friendId) {
        friendshipService.removeFriend(currentUserProvider.getCurrentUserId(), friendId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/friends")
    public ApiResponse<PageResponse<FriendshipResponse>> getFriends(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        return ApiResponse.<PageResponse<FriendshipResponse>>builder()
                .result(friendshipService.getFriends(currentUserProvider.getCurrentUserId(), page, size))
                .build();
    }

    @GetMapping("/sent-requests")
    public ApiResponse<PageResponse<FriendshipResponse>> getSentFriendRequests(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        return ApiResponse.<PageResponse<FriendshipResponse>>builder()
                .result(friendshipService.getSentFriendRequests(currentUserProvider.getCurrentUserId(), page, size))
                .build();
    }

    @GetMapping("/received-requests")
    public ApiResponse<PageResponse<FriendshipResponse>> getReceivedFriendRequests(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        return ApiResponse.<PageResponse<FriendshipResponse>>builder()
                .result(friendshipService.getReceivedFriendRequests(currentUserProvider.getCurrentUserId(), page, size))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<ProfileResponse>> searchFriends(@RequestParam("keyword") String keyword) {
        return ApiResponse.<List<ProfileResponse>>builder()
                .result(friendshipService.searchFriends(currentUserProvider.getCurrentUserId(), keyword))
                .build();
    }

    @GetMapping("/status/{friendId}")
    public ApiResponse<String> getFriendshipStatus(@PathVariable String friendId) {
        return ApiResponse.<String>builder()
                .result(friendshipService.getFriendshipStatus(currentUserProvider.getCurrentUserId(), friendId))
                .build();
    }

    @GetMapping("/mutual/{friendId}")
    public ApiResponse<List<ProfileResponse>> getMutualFriends(@PathVariable String friendId) {
        return ApiResponse.<List<ProfileResponse>>builder()
                .result(friendshipService.getMutualFriends(currentUserProvider.getCurrentUserId(), friendId))
                .build();
    }

    @GetMapping("/suggested")
    public ApiResponse<List<ProfileResponse>> getSuggestedFriends(
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit) {
        return ApiResponse.<List<ProfileResponse>>builder()
                .result(friendshipService.getSuggestedFriends(currentUserProvider.getCurrentUserId(), limit))
                .build();
    }

    @GetMapping("/pending-requests/count")
    public ApiResponse<Long> getPendingFriendRequestsCount() {
        return ApiResponse.<Long>builder()
                .result(friendshipService.getPendingFriendRequestsCount(currentUserProvider.getCurrentUserId()))
                .build();
    }

    @DeleteMapping("/{friendId}/cancel")
    public ApiResponse<Void> cancelFriendRequest(@PathVariable String friendId) {
        friendshipService.cancelFriendRequest(currentUserProvider.getCurrentUserId(), friendId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/sent-requests/count")
    public ApiResponse<Long> getSentFriendRequestsCount() {
        return ApiResponse.<Long>builder()
                .result(friendshipService.getSentFriendRequestsCount(currentUserProvider.getCurrentUserId()))
                .build();
    }

    @PostMapping("/batch-status")
    public ApiResponse<FriendshipStatusResponse> batchCheckFriendshipStatus(@RequestBody List<String> friendIds) {
        var statuses = friendshipService.batchCheckFriendshipStatus(currentUserProvider.getCurrentUserId(), friendIds);
        return ApiResponse.<FriendshipStatusResponse>builder()
                .result(FriendshipStatusResponse.builder().statuses(statuses).build())
                .build();
    }

    @GetMapping("/counts")
    public ApiResponse<SocialCountsResponse> getAllSocialCounts() {
        return ApiResponse.<SocialCountsResponse>builder()
                .result(friendshipService.getAllSocialCounts(currentUserProvider.getCurrentUserId()))
                .build();
    }
}
