package com.friendify.app.profile.controller;

import java.util.List;

import com.friendify.app.profile.dto.request.SearchUserRequest;
import com.friendify.app.profile.dto.request.UpdateProfileRequest;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.service.ProfileService;
import com.friendify.app.shared.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/profile/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileController {

    ProfileService profileService;

    @GetMapping("/{profileId}")
    public ApiResponse<ProfileResponse> getProfile(@PathVariable String profileId) {
        return ApiResponse.<ProfileResponse>builder()
                .result(profileService.getProfileById(profileId))
                .build();
    }

    @GetMapping
    public ApiResponse<List<ProfileResponse>> getAllProfiles() {
        return ApiResponse.<List<ProfileResponse>>builder()
                .result(profileService.getAllProfiles())
                .build();
    }

    @GetMapping("/my-profile")
    public ApiResponse<ProfileResponse> getMyProfile() {
        return ApiResponse.<ProfileResponse>builder()
                .result(profileService.getCurrentUserProfile())
                .build();
    }

    @PutMapping("/my-profile")
    public ApiResponse<ProfileResponse> updateMyProfile(@RequestBody UpdateProfileRequest request) {
        return ApiResponse.<ProfileResponse>builder()
                .result(profileService.updateCurrentUserProfile(request))
                .build();
    }

    @PostMapping("/search")
    public ApiResponse<List<ProfileResponse>> search(@RequestBody SearchUserRequest request) {
        return ApiResponse.<List<ProfileResponse>>builder()
                .result(profileService.search(request))
                .build();
    }

    @PutMapping("/avatar")
    public ApiResponse<ProfileResponse> updateAvatar(@RequestParam("file") MultipartFile file) {
        return ApiResponse.<ProfileResponse>builder()
                .result(profileService.updateAvatar(file))
                .build();
    }

    @PutMapping("/background")
    public ApiResponse<ProfileResponse> updateBackgroundImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.<ProfileResponse>builder()
                .result(profileService.updateBackgroundImage(file))
                .build();
    }
}
