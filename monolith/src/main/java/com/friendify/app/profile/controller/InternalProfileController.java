package com.friendify.app.profile.controller;

import java.util.List;

import com.friendify.app.profile.dto.request.ProfileCreationRequest;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.port.ProfileCreationPort;
import com.friendify.app.profile.port.ProfileQueryPort;
import com.friendify.app.shared.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalProfileController {

    ProfileCreationPort profileCreationPort;
    ProfileQueryPort profileQueryPort;

    @PostMapping
    public ApiResponse<ProfileResponse> createProfile(@RequestBody ProfileCreationRequest request) {
        return ApiResponse.<ProfileResponse>builder()
                .result(profileCreationPort.createProfile(request))
                .build();
    }

    @GetMapping("/{userId}")
    public ApiResponse<ProfileResponse> getProfile(@PathVariable String userId) {
        return ApiResponse.<ProfileResponse>builder()
                .result(profileQueryPort.getProfileByUserId(userId))
                .build();
    }

    @GetMapping("/batch")
    public ApiResponse<List<ProfileResponse>> getProfiles(@RequestParam("userIds") List<String> userIds) {
        return ApiResponse.<List<ProfileResponse>>builder()
                .result(profileQueryPort.getProfilesByUserIds(userIds))
                .build();
    }
}
