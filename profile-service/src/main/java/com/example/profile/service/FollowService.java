package com.example.profile.service;

import com.example.profile.client.AuthClient;
import com.example.profile.entity.Follow;
import com.example.profile.entity.Profile;
import com.example.profile.repository.FollowRepository;
import com.example.profile.repository.ProfileRepository;
import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowRepository followRepository;
    private final ProfileRepository profileRepository;
    private final AuthClientService authClientService;

    public boolean follow(String followeeId, HttpServletRequest httpServletRequest) {
        String email = authClientService.getUserEmail(httpServletRequest);

        return profileRepository.findByEmail(email)
                .map(Profile::getId)
                .map(followerId -> Follow.builder()
                        .followerProfile(profileRepository.findById(followerId).orElseThrow())
                        .followeeProfile(profileRepository.findById(followeeId).orElseThrow())
                        .followDateTime(LocalDateTime.now())
                        .build())
                .map(followRepository::save)
                .isPresent();
    }

    public boolean unfollow(String followeeId, HttpServletRequest httpServletRequest) {
        String email = authClientService.getUserEmail(httpServletRequest);

        return profileRepository.findByEmail(email)
                .map(Profile::getId)
                .map(followerId ->
                        followRepository.deleteByFollowerProfile_IdAndFolloweeProfile_Id(followerId, followeeId))
                .isPresent();
    }

    public List<Profile> getFollowers(String profileId) {
        return followRepository.findAllByFolloweeProfile_Id(profileId)
                .stream()
                .map(Follow::getFolloweeProfile)
                .toList();
    }

    public List<Profile> getFollowees(String profileId) {
        return followRepository.findAllByFollowerProfile_Id(profileId)
                .stream()
                .map(Follow::getFollowerProfile)
                .toList();
    }

    public boolean isFollowed(String followeeId, HttpServletRequest httpServletRequest) {
        String email = authClientService.getUserEmail(httpServletRequest);

        return profileRepository.findByEmail(email)
                .map(Profile::getId)
                .map(followerId -> followRepository.existsByFollowerProfile_IdAndFolloweeProfile_Id(followerId, followeeId))
                .orElseThrow();
    }
}
