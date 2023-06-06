package com.example.profile.service;

import com.example.profile.client.StorageServiceClient;
import com.example.profile.dto.request.CreateProfileRequest;
import com.example.profile.dto.request.UpdateProfileRequest;
import com.example.profile.dto.response.ProfileResponse;
import com.example.profile.entity.Profile;
import com.example.profile.exception.ActionNotAllowedException;
import com.example.profile.exception.EntityNotFoundException;
import com.example.profile.mapper.ProfileMapper;
import com.example.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final MessageSourceService messageSourceService;
    private final StorageServiceClient storageServiceClient;

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
                .filter(profile -> checkUpdateAvailabilityForUser(profile.getEmail(), loggedInUser))
                .map(profile -> profileMapper.updateProfileFromUpdateProfileRequest(updateProfileRequest, profile))
                .map(profileRepository::save)
                .map(profileMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", id)
                ));
    }

    public String getProfileIdByEmail(String email) {
        return profileRepository.findByEmail(email)
                .map(Profile::getId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", email)
                ));
    }

    private boolean checkUpdateAvailabilityForUser(String updatingUser, String loggedInUser) {
        if (!updatingUser.equals(loggedInUser)) {
            throw new ActionNotAllowedException(
                    messageSourceService.generateMessage("error.forbidden")
            );
        }
        return true;
    }

    public Boolean uploadAvatarImage(MultipartFile file, String loggedInUser) {
        return profileRepository.findByEmail(loggedInUser)
                .map(profile -> {
                    if (profile.getAvatarUrl() != null) {
                        storageServiceClient.deleteFile(profile.getAvatarUrl());
                    }
                    String url = storageServiceClient.uploadFile(file);
                    profile.setAvatarUrl(url);
                    profileRepository.save(profile);
                    return profile.getAvatarUrl() != null;
                })
                .orElse(false);
    }

    public Boolean deleteAvatarImage(String loggedInUser) {
        return profileRepository.findByEmail(loggedInUser)
                .filter(profile -> profile.getAvatarUrl() != null)
                .map(profile -> {
                    String avatarUrl = profile.getAvatarUrl();
                    profile.setAvatarUrl(null);
                    profileRepository.save(profile);
                    return avatarUrl;
                })
                .map(storageServiceClient::deleteFile)
                .isPresent();
    }

    public Boolean uploadBannerImage(MultipartFile file, String loggedInUser) {
        return profileRepository.findByEmail(loggedInUser)
                .map(profile -> {
                    if (profile.getBannerUrl() != null) {
                        storageServiceClient.deleteFile(profile.getBannerUrl());
                    }
                    String url = storageServiceClient.uploadFile(file);
                    profile.setBannerUrl(url);
                    profileRepository.save(profile);
                    return profile.getBannerUrl() != null;
                })
                .orElse(false);
    }

    public Boolean deleteBannerImage(String loggedInUser) {
        return profileRepository.findByEmail(loggedInUser)
                .filter(profile -> profile.getBannerUrl() != null)
                .map(profile -> {
                    String bannerUrl = profile.getBannerUrl();
                    profile.setBannerUrl(null);
                    profileRepository.save(profile);
                    return bannerUrl;
                })
                .map(storageServiceClient::deleteFile)
                .isPresent();
    }
}
