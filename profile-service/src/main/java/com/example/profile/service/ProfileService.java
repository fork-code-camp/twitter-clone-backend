package com.example.profile.service;

import com.example.profile.client.StorageServiceClient;
import com.example.profile.dto.filter.ProfileFilter;
import com.example.profile.dto.request.CreateProfileRequest;
import com.example.profile.dto.request.UpdateProfileRequest;
import com.example.profile.dto.response.ProfileResponse;
import com.example.profile.entity.Profile;
import com.example.profile.exception.ActionNotAllowedException;
import com.example.profile.exception.EntityNotFoundException;
import com.example.profile.mapper.ProfileMapper;
import com.example.profile.repository.ProfileRepository;
import com.example.profile.util.FollowsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static com.example.profile.constant.CacheName.PROFILES_CACHE;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final MessageSourceService messageSourceService;
    private final FollowsUtil followsUtil;
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

    @Cacheable(cacheNames = PROFILES_CACHE, key = "#p0")
    public ProfileResponse getProfile(String id) {
        return profileRepository.findById(id)
                .map(profile -> profileMapper.toResponse(profile, followsUtil))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", id)
                ));
    }

    @CachePut(cacheNames = PROFILES_CACHE, key = "#p0")
    public ProfileResponse updateProfile(String id, UpdateProfileRequest updateProfileRequest, String loggedInUser) {
        return profileRepository.findById(id)
                .filter(profile -> checkUpdateAvailabilityForUser(profile.getEmail(), loggedInUser))
                .map(profile -> profileMapper.updateProfileFromUpdateProfileRequest(updateProfileRequest, profile))
                .map(profileRepository::save)
                .map(profile -> profileMapper.toResponse(profile, followsUtil))
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

    public String getProfileAvatar(String loggedInUser) {
        return profileRepository.findByEmail(loggedInUser)
                .map(Profile::getAvatarUrl)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.image.not_found", loggedInUser)
                ));
    }

    public String getProfileBanner(String loggedInUser) {
        return profileRepository.findByEmail(loggedInUser)
                .map(Profile::getBannerUrl)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.image.not_found", loggedInUser)
                ));
    }

    public Page<ProfileResponse> findAllByUsername(ProfileFilter filter, Pageable pageable) {
        return profileRepository.findByUsernameContaining(filter.username(), pageable)
                .map(profile -> profileMapper.toResponse(profile, followsUtil));
    }

    public ProfileResponse getAuthProfile(String loggedInUser) {
        return profileRepository.findByEmail(loggedInUser)
                .map(profile -> profileMapper.toResponse(profile, followsUtil))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", loggedInUser)
                ));
    }
}
