package com.friendify.app.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.friendify.app.profile.dto.request.ProfileCreationRequest;
import com.friendify.app.profile.dto.request.SearchUserRequest;
import com.friendify.app.profile.dto.request.UpdateProfileRequest;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.entity.Profile;
import com.friendify.app.profile.mapper.ProfileMapper;
import com.friendify.app.profile.port.ProfileCreationPort;
import com.friendify.app.profile.port.ProfileQueryPort;
import com.friendify.app.profile.repository.ProfileRepository;
import com.friendify.app.shared.security.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTests {

    @Mock
    ProfileRepository profileRepository;

    @Mock
    ProfileMapper profileMapper;

    @Mock
    CurrentUserProvider currentUserProvider;

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
}
