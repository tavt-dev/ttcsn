package com.friendify.app.profile.mapper;

import com.friendify.app.profile.dto.request.ProfileCreationRequest;
import com.friendify.app.profile.dto.request.UpdateProfileRequest;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.entity.Profile;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    Profile toProfile(ProfileCreationRequest request);

    ProfileResponse toProfileResponse(Profile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Profile profile, UpdateProfileRequest request);
}
