package com.example.profile.mapper;

import com.example.profile.dto.request.ProfileRequest;
import com.example.profile.entity.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    Profile toEntity(ProfileRequest profileRequest);
}
