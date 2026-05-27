package com.friendify.app.profile.service;

import java.util.List;

import com.friendify.app.profile.dto.request.ProfileCreationRequest;
import com.friendify.app.profile.dto.request.SearchUserRequest;
import com.friendify.app.profile.dto.request.UpdateProfileRequest;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.entity.Profile;
import com.friendify.app.profile.mapper.ProfileMapper;
import com.friendify.app.profile.port.ProfileCreationPort;
import com.friendify.app.profile.port.ProfileQueryPort;
import com.friendify.app.profile.repository.ProfileRepository;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.friendify.app.shared.security.CurrentUserProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileService implements ProfileCreationPort, ProfileQueryPort {

    ProfileRepository profileRepository;
    ProfileMapper profileMapper;
    CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ProfileResponse createProfile(ProfileCreationRequest request) {
        return profileRepository.findByUserId(request.getUserId())
                .map(profileMapper::toProfileResponse)
                .orElseGet(() -> {
                    Profile profile = profileMapper.toProfile(request);
                    return profileMapper.toProfileResponse(profileRepository.save(profile));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfileById(String profileId) {
        return profileMapper.toProfileResponse(findById(profileId));
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfileByUserId(String userId) {
        return profileMapper.toProfileResponse(findByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getCurrentUserProfile() {
        return getProfileByUserId(currentUserProvider.getCurrentUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileResponse> getAllProfiles() {
        return profileRepository.findAll().stream()
                .map(profileMapper::toProfileResponse)
                .toList();
    }

    @Transactional
    public ProfileResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        Profile profile = findByUserId(currentUserProvider.getCurrentUserId());
        profileMapper.update(profile, request);
        return profileMapper.toProfileResponse(profileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileResponse> search(SearchUserRequest request) {
        String currentUserId = currentUserProvider.getCurrentUserId();
        String keyword = request.getKeyword() == null ? "" : request.getKeyword().trim();
        if (keyword.isEmpty()) {
            return List.of();
        }

        return profileRepository.searchByKeyword(keyword).stream()
                .filter(profile -> !currentUserId.equals(profile.getUserId()))
                .map(profileMapper::toProfileResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileResponse> getProfilesByUserIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        return profileRepository.findAllByUserIdIn(userIds).stream()
                .map(profileMapper::toProfileResponse)
                .toList();
    }

    private Profile findById(String profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private Profile findByUserId(String userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
