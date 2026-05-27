package com.friendify.app.profile.port;

import java.util.List;

import com.friendify.app.profile.dto.request.SearchUserRequest;
import com.friendify.app.profile.dto.response.ProfileResponse;

public interface ProfileQueryPort {
    ProfileResponse getProfileById(String profileId);

    ProfileResponse getProfileByUserId(String userId);

    ProfileResponse getCurrentUserProfile();

    List<ProfileResponse> getAllProfiles();

    List<ProfileResponse> getProfilesByUserIds(List<String> userIds);

    List<ProfileResponse> search(SearchUserRequest request);
}
