package com.example.profile.service;

import com.example.profile.dto.response.ProfileResponse;
import com.example.profile.entity.Follow;
import com.example.profile.entity.Profile;
import com.example.profile.mapper.ProfileMapper;
import com.example.profile.repository.FollowRepository;
import com.example.profile.repository.ProfileRepository;
import com.example.profile.util.FollowsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final FollowsUtil followsUtil;

    public boolean follow(String followeeId, String loggedInUser) {
        return profileRepository.findByEmail(loggedInUser)
                .map(Profile::getId)
                .filter(followerId -> !isFollowed(followeeId, loggedInUser))
                .map(followerId -> Follow.builder()
                        .followerProfile(profileRepository.findById(followerId).orElseThrow())
                        .followeeProfile(profileRepository.findById(followeeId).orElseThrow())
                        .followDateTime(LocalDateTime.now())
                        .build())
                .map(followRepository::save)
                .isPresent();
    }

    public boolean unfollow(String followeeId, String loggedInUser) {
        return profileRepository.findByEmail(loggedInUser)
                .map(Profile::getId)
                .filter(followerId -> isFollowed(followeeId, loggedInUser))
                .map(followerId ->
                        followRepository.deleteByFollowerProfile_IdAndFolloweeProfile_Id(followerId, followeeId))
                .isPresent();
    }

    @Cacheable(cacheNames = "followers", key = "#p0", unless = "#result.size() < 10000")
    public List<ProfileResponse> getFollowers(String profileId) {
        return followRepository.findAllByFolloweeProfile_Id(profileId)
                .stream()
                .map(Follow::getFollowerProfile)
                .map(profile -> profileMapper.toResponse(profile, followsUtil))
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "followees", key = "#p0", unless = "#result.size() < 1000")
    public List<ProfileResponse> getFollowees(String profileId) {
        return followRepository.findAllByFollowerProfile_Id(profileId)
                .stream()
                .map(Follow::getFolloweeProfile)
                .map(profile -> profileMapper.toResponse(profile, followsUtil))
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "followees_celebrities", key = "#p0")
    public List<ProfileResponse> getFolloweesCelebrities(String profileId) {
        return getFollowees(profileId)
                .stream()
                .filter(followee -> followee.followers() > 10000)
                .collect(Collectors.toList());
    }

    public boolean isFollowed(String followeeId, String loggedInUser) {
        return profileRepository.findByEmail(loggedInUser)
                .map(Profile::getId)
                .map(followerId -> followRepository.existsByFollowerProfile_IdAndFolloweeProfile_Id(followerId, followeeId))
                .orElse(false);
    }
}
