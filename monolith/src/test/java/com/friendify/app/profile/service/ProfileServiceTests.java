package com.friendify.app.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.friendify.app.file.dto.UploadResponse;
import com.friendify.app.file.port.FileUploadPort;
import com.friendify.app.profile.dto.request.ProfileCreationRequest;
import com.friendify.app.profile.dto.request.SearchUserRequest;
import com.friendify.app.profile.dto.request.UpdateProfileRequest;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.entity.Profile;
import com.friendify.app.profile.mapper.ProfileMapper;
import com.friendify.app.profile.port.ProfileCreationPort;
import com.friendify.app.profile.port.ProfileQueryPort;
import com.friendify.app.profile.repository.ProfileRepository;
import com.friendify.app.shared.media.ImageType;
import com.friendify.app.shared.security.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTests {

    @Mock
    ProfileRepository profileRepository;

    @Mock
    ProfileMapper profileMapper;

    @Mock
    CurrentUserProvider currentUserProvider;

    @Mock
    FileUploadPort fileUploadPort;

    @InjectMocks
    ProfileService profileService;

    @Test
    void implementsProfilePorts() {
        assertThat(profileService).isInstanceOf(ProfileCreationPort.class);
        assertThat(profileService).isInstanceOf(ProfileQueryPort.class);
    }

    @Test
    void createProfileReturnsExistingProfileWithoutSavingDuplicate() {
        ProfileCreationRequest request = ProfileCreationRequest.builder()
                .userId("user-1")
                .username("alice")
                .build();
        Profile existingProfile = Profile.builder()
                .id("profile-1")
                .userId("user-1")
                .username("alice")
                .build();
        ProfileResponse response = ProfileResponse.builder()
                .id("profile-1")
                .userId("user-1")
                .username("alice")
                .build();

        when(profileRepository.findByUserId("user-1")).thenReturn(Optional.of(existingProfile));
        when(profileMapper.toProfileResponse(existingProfile)).thenReturn(response);

        ProfileResponse result = profileService.createProfile(request);

        assertThat(result).isSameAs(response);
        verify(profileRepository, never()).save(existingProfile);
    }

    @Test
    void updateCurrentUserProfileUpdatesTextFields() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .bio("Updated bio")
                .city("Da Nang")
                .build();
        Profile profile = Profile.builder()
                .id("profile-1")
                .userId("user-1")
                .build();
        Profile savedProfile = Profile.builder()
                .id("profile-1")
                .userId("user-1")
                .bio("Updated bio")
                .city("Da Nang")
                .build();
        ProfileResponse response = ProfileResponse.builder()
                .id("profile-1")
                .userId("user-1")
                .bio("Updated bio")
                .city("Da Nang")
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn("user-1");
        when(profileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));
        when(profileRepository.save(profile)).thenReturn(savedProfile);
        when(profileMapper.toProfileResponse(savedProfile)).thenReturn(response);

        ProfileResponse result = profileService.updateCurrentUserProfile(request);

        assertThat(result).isSameAs(response);
        verify(profileMapper).update(profile, request);
    }

    @Test
    void searchTrimsKeywordAndExcludesCurrentUserProfile() {
        SearchUserRequest request = SearchUserRequest.builder()
                .keyword(" alice ")
                .build();
        Profile ownProfile = Profile.builder()
                .id("profile-1")
                .userId("user-1")
                .username("alice")
                .build();
        Profile otherProfile = Profile.builder()
                .id("profile-2")
                .userId("user-2")
                .username("alice2")
                .build();
        ProfileResponse otherResponse = ProfileResponse.builder()
                .id("profile-2")
                .userId("user-2")
                .username("alice2")
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn("user-1");
        when(profileRepository.searchByKeyword("alice")).thenReturn(List.of(ownProfile, otherProfile));
        when(profileMapper.toProfileResponse(otherProfile)).thenReturn(otherResponse);

        List<ProfileResponse> result = profileService.search(request);

        assertThat(result).containsExactly(otherResponse);
        verify(profileMapper, never()).toProfileResponse(ownProfile);
    }

    @Test
    void updateAvatarUploadsThroughFileUploadPort() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "avatar".getBytes());
        Profile profile = Profile.builder()
                .id("profile-1")
                .userId("user-1")
                .build();
        Profile savedProfile = Profile.builder()
                .id("profile-1")
                .userId("user-1")
                .avatar("https://cdn.example/avatar.png")
                .build();
        ProfileResponse response = ProfileResponse.builder()
                .id("profile-1")
                .userId("user-1")
                .avatar("https://cdn.example/avatar.png")
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn("user-1");
        when(profileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));
        when(fileUploadPort.uploadImage(eq(file), eq(ImageType.AVATAR), eq("user-1"), eq(null)))
                .thenReturn(new UploadResponse("public-id", 1L, 100, 100, "https://cdn.example/avatar.png", null));
        when(profileRepository.save(profile)).thenReturn(savedProfile);
        when(profileMapper.toProfileResponse(savedProfile)).thenReturn(response);

        ProfileResponse result = profileService.updateAvatar(file);

        assertThat(result).isSameAs(response);
        assertThat(profile.getAvatar()).isEqualTo("https://cdn.example/avatar.png");
        verify(fileUploadPort).uploadImage(eq(file), eq(ImageType.AVATAR), eq("user-1"), eq(null));
    }

    @Test
    void updateBackgroundUploadsThroughFileUploadPort() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "background.png",
                "image/png",
                "background".getBytes());
        Profile profile = Profile.builder()
                .id("profile-1")
                .userId("user-1")
                .build();
        Profile savedProfile = Profile.builder()
                .id("profile-1")
                .userId("user-1")
                .backgroundImage("https://cdn.example/background.png")
                .build();
        ProfileResponse response = ProfileResponse.builder()
                .id("profile-1")
                .userId("user-1")
                .backgroundImage("https://cdn.example/background.png")
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn("user-1");
        when(profileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));
        when(fileUploadPort.uploadImage(eq(file), eq(ImageType.BACKGROUND_IMAGE), eq("user-1"), eq(null)))
                .thenReturn(new UploadResponse("public-id", 1L, 100, 100, "https://cdn.example/background.png", null));
        when(profileRepository.save(profile)).thenReturn(savedProfile);
        when(profileMapper.toProfileResponse(savedProfile)).thenReturn(response);

        ProfileResponse result = profileService.updateBackgroundImage(file);

        assertThat(result).isSameAs(response);
        assertThat(profile.getBackgroundImage()).isEqualTo("https://cdn.example/background.png");
        verify(fileUploadPort).uploadImage(eq(file), eq(ImageType.BACKGROUND_IMAGE), eq("user-1"), eq(null));
    }
}
