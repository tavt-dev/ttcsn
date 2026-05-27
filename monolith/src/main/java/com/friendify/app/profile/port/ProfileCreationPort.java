package com.friendify.app.profile.port;

import com.friendify.app.profile.dto.request.ProfileCreationRequest;
import com.friendify.app.profile.dto.response.ProfileResponse;

public interface ProfileCreationPort {
    ProfileResponse createProfile(ProfileCreationRequest request);
}
