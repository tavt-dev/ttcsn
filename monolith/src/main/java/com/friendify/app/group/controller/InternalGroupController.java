package com.friendify.app.group.controller;

import com.friendify.app.group.dto.response.GroupResponse;
import com.friendify.app.group.service.GroupService;
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
public class InternalGroupController {
    GroupService groupService;

    @GetMapping("/groups/{groupId}/exists")
    ApiResponse<Boolean> checkGroupExists(@PathVariable String groupId) {
        return ApiResponse.<Boolean>builder()
                .code(200)
                .message("Check group exists")
                .result(groupService.checkGroupExists(groupId))
                .build();
    }

    @GetMapping("/groups/{groupId}")
    ApiResponse<GroupResponse> getGroup(@PathVariable String groupId) {
        return ApiResponse.<GroupResponse>builder()
                .result(groupService.getGroup(groupId))
                .build();
    }

    @GetMapping("/groups/{groupId}/can-post")
    ApiResponse<Boolean> canPost(@PathVariable String groupId) {
        return ApiResponse.<Boolean>builder()
                .result(groupService.canPost(groupId))
                .build();
    }

    @GetMapping("/groups/{groupId}/can-view")
    ApiResponse<Boolean> canViewPosts(@PathVariable String groupId) {
        return ApiResponse.<Boolean>builder()
                .result(groupService.canViewPosts(groupId))
                .build();
    }

    @GetMapping("/groups/{groupId}/can-view/{userId}")
    ApiResponse<Boolean> canViewPostsInternal(
            @PathVariable String groupId,
            @PathVariable String userId) {
        return ApiResponse.<Boolean>builder()
                .result(groupService.canView(groupId, userId))
                .build();
    }
}
