package com.example.profile.util;

import com.example.profile.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowsUtil {

    private final FollowRepository followRepository;

    public int countFollowersForProfile(String profileId) {
        return followRepository.countAllByFolloweeProfile_Id(profileId);
    }

    public int countFolloweesForProfile(String profileId) {
        return followRepository.countAllByFollowerProfile_Id(profileId);
    }
}
