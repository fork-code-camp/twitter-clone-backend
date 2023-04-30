package com.example.profile.service;

import com.example.profile.dto.request.CreateProfileRequest;
import com.example.profile.dto.request.UpdateProfileRequest;
import com.example.profile.dto.response.ProfileResponse;
import com.example.profile.entity.Profile;
import com.example.profile.mapper.ProfileMapper;
import com.example.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    public String createProfile(CreateProfileRequest createProfileRequest) {
        return Optional.of(createProfileRequest)
                .map(profileMapper::toEntity)
                .map(profileRepository::save)
                .map(Profile::getId)
                .orElseThrow();
    }

    public ProfileResponse getProfile(String id) {
        return profileRepository.findById(id)
                .map(profileMapper::toResponse)
                .orElseThrow();
    }

    public ProfileResponse updateProfile(String id, UpdateProfileRequest updateProfileRequest) {
        return profileRepository.findById(id)
                .map(profile -> {
                    profileMapper.updateProfileFromUpdateProfileRequest(updateProfileRequest, profile);
                    profileRepository.save(profile);
                    return profile;
                })
                .map(profileMapper::toResponse)
                .orElseThrow();
    }
}
