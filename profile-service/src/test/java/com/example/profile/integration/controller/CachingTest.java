package com.example.profile.integration.controller;

import com.example.profile.dto.request.UpdateProfileRequest;
import com.example.profile.dto.response.ProfileResponse;
import com.example.profile.entity.Follow;
import com.example.profile.entity.Profile;
import com.example.profile.integration.IntegrationTestBase;
import com.example.profile.repository.FollowRepository;
import com.example.profile.repository.ProfileRepository;
import com.example.profile.service.FollowService;
import com.example.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.example.profile.constant.CacheName.*;
import static com.example.profile.integration.constants.ProfileConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;


@RequiredArgsConstructor
@SuppressWarnings("SameParameterValue")
public class CachingTest extends IntegrationTestBase {

    private final FollowService followService;
    private final ProfileService profileService;
    @MockBean
    private final FollowRepository followRepository;
    @MockBean
    private final ProfileRepository profileRepository;

    @Test
    public void cacheProfileTest() {
        createStubForProfile(ID.getConstant());

        profileService.getProfile(ID.getConstant());
        ProfileResponse profile = profileService.getProfile(ID.getConstant());

        verify(profileRepository, times(1)).findById(ID.getConstant());

        ProfileResponse profileFromCache = getProfileFromCache(ID.getConstant());
        assertNotNull(profileFromCache);
        assertEquals(profile, profileFromCache);
    }

    @Test
    public void updateProfileInCacheTest() {
        createStubForProfile(ID.getConstant());

        ProfileResponse profile = profileService.getProfile(ID.getConstant());
        ProfileResponse profileFromCache = getProfileFromCache(ID.getConstant());
        assertEquals(profile, profileFromCache);

        ProfileResponse updatedProfile = profileService.updateProfile(ID.getConstant(), buildUpdateProfileRequest(), NEW_PROFILE_EMAIL.getConstant());
        profileFromCache = getProfileFromCache(ID.getConstant());
        assertEquals(updatedProfile, profileFromCache);
    }

    @Test
    public void cacheFollowersTest() {
        createStubForProfileWithFollowersAndFollowees(ID.getConstant(), 10000, 10, 0);

        followService.getFollowers(ID.getConstant());
        followService.getFollowers(ID.getConstant());

        verify(followRepository, times(1))
                .findAllByFolloweeProfile_Id(ID.getConstant());

        List<ProfileResponse> followers = getProfilesFromCache(ID.getConstant(), FOLLOWERS_CACHE);
        assertNotNull(followers);
        assertEquals(10000, followers.size());
    }

    @Test
    public void cacheFolloweesTest() {
        createStubForProfileWithFollowersAndFollowees(ID.getConstant(), 10, 1000, 0);

        followService.getFollowees(ID.getConstant());
        followService.getFollowees(ID.getConstant());

        verify(followRepository, times(1))
                .findAllByFollowerProfile_Id(ID.getConstant());

        List<ProfileResponse> followees = getProfilesFromCache(ID.getConstant(), FOLLOWEES_CACHE);
        assertNotNull(followees);
        assertEquals(1000, followees.size());
    }

    @Test
    public void cacheFolloweesCelebritiesTest() {
        createStubForProfileWithFollowersAndFollowees(ID.getConstant(), 10, 100, 50);

        followService.getFolloweesCelebrities(ID.getConstant());
        followService.getFolloweesCelebrities(ID.getConstant());

        verify(followRepository, times(1))
                .findAllByFollowerProfile_Id(ID.getConstant());

        List<ProfileResponse> followeesCelebrities = getProfilesFromCache(ID.getConstant(), FOLLOWEES_CELEBRITIES_CACHE);
        assertNotNull(followeesCelebrities);
        assertEquals(50, followeesCelebrities.size());
    }

    private void createStubForProfileWithFollowersAndFollowees(String profileId, int followers, int followees, int followeesCelebrities) {
        Profile parentProfile = createStubForProfile(profileId);
        createStubForFollowersList(followers, parentProfile);
        createStubForFolloweesList(followees, followeesCelebrities, parentProfile);
    }

    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    private void createStubForFollowersList(int followers, Profile parentProfile) {
        List<Follow> followersListFromDb = mock(ArrayList.class);
        List<ProfileResponse> followersList = mock(ArrayList.class);
        Stream<Follow> mockStreamOfFollows = mock(Stream.class);

        when(followRepository.findAllByFolloweeProfile_Id(parentProfile.getId()))
                .thenReturn(followersListFromDb);

        when(followersListFromDb.stream())
                .thenReturn(mockStreamOfFollows);

        when(mockStreamOfFollows.map(any(Function.class)))
                .thenReturn(mockStreamOfFollows);

        when(mockStreamOfFollows.collect(any()))
                .thenReturn(followersList);

        when(followersList.size())
                .thenReturn(followers);
    }

    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    private void createStubForFolloweesList(int followees, int celebrities, Profile parentProfile) {
        List<Follow> followeesListFromDb = mock(ArrayList.class);
        List<ProfileResponse> followeesList = mock(ArrayList.class);
        Stream<Follow> mockStreamOfFollows = mock(Stream.class);

        when(followRepository.findAllByFollowerProfile_Id(parentProfile.getId()))
                .thenReturn(followeesListFromDb);

        when(followeesListFromDb.stream())
                .thenReturn(mockStreamOfFollows);

        when(mockStreamOfFollows.map(any(Function.class)))
                .thenReturn(mockStreamOfFollows);

        when(mockStreamOfFollows.collect(any()))
                .thenReturn(followeesList);

        when(followeesList.size())
                .thenReturn(followees);

        if (celebrities > 0) {
            createStubForFolloweesCelebritiesList(celebrities, followeesList);
        }
    }

    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    private void createStubForFolloweesCelebritiesList(int celebrities, List<ProfileResponse> followeesList) {
        List<ProfileResponse> celebritiesList = mock(ArrayList.class);
        Stream<ProfileResponse> mockStreamOfCelebrities = mock(Stream.class);

        when(followeesList.stream())
                .thenReturn(mockStreamOfCelebrities);

        when(mockStreamOfCelebrities.filter(any()))
                .thenReturn(mockStreamOfCelebrities);

        when(mockStreamOfCelebrities.collect(any()))
                .thenReturn(celebritiesList);

        when(celebritiesList.size())
                .thenReturn(celebrities);
    }

    private Profile createStubForProfile(String profileId) {
        Profile profile = buildDefaultProfile(profileId);

        when(profileRepository.findById(profileId))
                .thenReturn(Optional.of(profile));

        when(profileRepository.save(profile))
                .thenReturn(profile);

        return profile;
    }

    @Nullable
    @SuppressWarnings("DataFlowIssue")
    private ProfileResponse getProfileFromCache(String profileId) {
        return cacheManager.getCache("profiles").get(profileId, ProfileResponse.class);
    }

    @Nullable
    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    private List<ProfileResponse> getProfilesFromCache(String parentProfileId, String cacheName) {
        return cacheManager.getCache(cacheName).get(parentProfileId, List.class);
    }

    @NonNull
    private UpdateProfileRequest buildUpdateProfileRequest() {
        return new UpdateProfileRequest(
                "new username",
                "new bio",
                "new location",
                "new website",
                LocalDate.of(2000, 1, 1)
        );
    }

    private Profile buildDefaultProfile(String profileId) {
        return Profile.builder()
                .id(profileId)
                .email(NEW_PROFILE_EMAIL.getConstant())
                .username(USERNAME.getConstant())
                .joinDate(LocalDate.now())
                .build();
    }
}
