package com.example.profile.mapper;

import com.example.profile.dto.request.CreateProfileRequest;
import com.example.profile.dto.request.UpdateProfileRequest;
import com.example.profile.dto.response.ProfileResponse;
import com.example.profile.entity.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    Profile toEntity(CreateProfileRequest createProfileRequest);

    ProfileResponse toResponse(Profile profile);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "email", ignore = true),
            @Mapping(target = "joinDate", ignore = true)
    })
    void updateProfileFromUpdateProfileRequest(UpdateProfileRequest updateProfileRequest, @MappingTarget Profile profile);
}
