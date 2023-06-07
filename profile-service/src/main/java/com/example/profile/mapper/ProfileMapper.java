package com.example.profile.mapper;

import com.example.profile.dto.request.CreateProfileRequest;
import com.example.profile.dto.request.UpdateProfileRequest;
import com.example.profile.dto.response.ProfileResponse;
import com.example.profile.entity.Profile;
import com.example.profile.util.FollowsUtil;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    Profile toEntity(CreateProfileRequest createProfileRequest);

    @Mapping(target = "profileId", source = "id")
    @Mapping(target = "followees", expression = "java(followsUtil.countFolloweesForProfile(profile.getId()))")
    @Mapping(target = "followers", expression = "java(followsUtil.countFollowersForProfile(profile.getId()))")
    ProfileResponse toResponse(Profile profile, @Context FollowsUtil followsUtil);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "email", ignore = true),
            @Mapping(target = "joinDate", ignore = true)
    })
    Profile updateProfileFromUpdateProfileRequest(UpdateProfileRequest updateProfileRequest, @MappingTarget Profile profile);
}
