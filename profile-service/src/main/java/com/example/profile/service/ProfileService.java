package com.example.profile.service;

import com.example.profile.dto.request.CreateProfileRequest;
import com.example.profile.dto.request.UpdateProfileRequest;
import com.example.profile.dto.response.ProfileResponse;
import com.example.profile.entity.Profile;
import com.example.profile.exception.EntityNotFoundException;
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
    private final MessageSourceService messageSourceService;

    public String createProfile(CreateProfileRequest createProfileRequest) {
        return Optional.of(createProfileRequest)
                .map(profileMapper::toEntity)
                .map(profileRepository::save)
                .map(Profile::getId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.unsuccessful_creation")
                ));
    }

    public ProfileResponse getProfile(String id) {
        return profileRepository.findById(id)
                .map(profileMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", id)
                ));
    }

    public ProfileResponse updateProfile(String id, UpdateProfileRequest updateProfileRequest, String loggedInUser) {
        return profileRepository.findById(id)
                .filter(profile -> profile.getEmail().equals(loggedInUser))
                .map(profile -> {
                    profileMapper.updateProfileFromUpdateProfileRequest(updateProfileRequest, profile);
                    profileRepository.save(profile);
                    return profile;
                })
                .map(profileMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Entity " + id + " not found")); // TODO: message source
    }

    public String getProfileIdByEmail(String email) {
        return profileRepository.findByEmail(email)
                .map(Profile::getId)
                .orElseThrow(() -> new EntityNotFoundException("Profile with email " + email + " not found")); // TODO: message source
    }
}
