package com.example.profile.service;

import com.example.profile.client.AuthClient;
import com.example.profile.entity.Follow;
import com.example.profile.entity.Profile;
import com.example.profile.exception.MissingTokenException;
import com.example.profile.exception.NonAuthorizedException;
import com.example.profile.repository.FollowRepository;
import com.example.profile.repository.ProfileRepository;
import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final ProfileRepository profileRepository;
    private final AuthClient authClient;

    public boolean follow(String followeeId, HttpServletRequest httpServletRequest) {
        String email = getUserEmail(httpServletRequest);
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
        String email = getUserEmail(httpServletRequest);
        return profileRepository.findByEmail(email)
                .map(Profile::getId)
                .map(followerId ->
                        followRepository.deleteByFollowerProfile_IdAndFolloweeProfile_Id(followerId, followeeId))
                .isPresent();
    }

    public List<Profile> getFollowers(String profileId) {
        return followRepository.findAllByFolloweeProfile_Id(profileId)
                .stream()
                .map(Follow::getFollowerProfile)
                .toList();
    }

    public List<Profile> getFollowees(String profileId) {
        return followRepository.findAllByFollowerProfile_Id(profileId)
                .stream()
                .map(Follow::getFolloweeProfile)
                .toList();
    }

    public boolean isFollowed(String followeeId, HttpServletRequest httpServletRequest) {
        String email = getUserEmail(httpServletRequest);
        return profileRepository.findByEmail(email)
                .map(Profile::getId)
                .map(followerId -> followRepository.existsByFollowerProfile_IdAndFolloweeProfile_Id(followerId, followeeId))
                .orElseThrow();
    }

    private String getUserEmail(HttpServletRequest httpServletRequest) {
        String bearerToken = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken == null || StringUtils.isEmpty(bearerToken) || StringUtils.isBlank(bearerToken)) {
            throw new MissingTokenException(
                    "You don't have authentication token. Please, authenticate and try again."
            );
        }

        String email = authClient.getPrincipalUsername(bearerToken);
        log.info("Email {} has been successfully extracted", email);

        if (email == null) {
            throw new NonAuthorizedException(
                    "You are not authorized."
            );
        }

        return email;
    }
}
