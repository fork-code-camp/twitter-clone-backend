package com.example.profile.integration.controller;

import com.example.profile.client.StorageServiceClient;
import com.example.profile.dto.request.UpdateProfileRequest;
import com.example.profile.dto.response.ProfileResponse;
import com.example.profile.entity.Follow;
import com.example.profile.entity.Profile;
import com.example.profile.integration.IntegrationTestBase;
import com.example.profile.mapper.ProfileMapper;
import com.example.profile.repository.FollowRepository;
import com.example.profile.repository.ProfileRepository;
import com.example.profile.service.FollowService;
import com.example.profile.service.ProfileService;
import com.example.profile.util.FollowsUtil;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.example.profile.integration.constants.ProfileConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@RequiredArgsConstructor
@SuppressWarnings("all")
public class CachingTest extends IntegrationTestBase {

    private final CacheManager cacheManager;
    private final FollowService followService;
    private final ProfileService profileService;
    private final ProfileMapper profileMapper;

    @MockBean
    private final FollowRepository followRepository;

    @MockBean
    private final ProfileRepository profileRepository;

    @MockBean
    private final FollowsUtil followsUtil;

    @MockBean
    private final StorageServiceClient storageServiceClient;

    @BeforeEach
    public void setUp() {
        cacheManager.getCache("profiles").clear();
        cacheManager.getCache("followers").clear();
        cacheManager.getCache("followees").clear();
        cacheManager.getCache("followees_celebrities").clear();
    }

    @Test
    public void cacheProfileTest() {
        createStubForProfile(ID.getConstant(), 10000, 100);

        profileService.getProfile(ID.getConstant());
        ProfileResponse profile = profileService.getProfile(ID.getConstant());

        verify(profileRepository, times(1)).findById(ID.getConstant());

        ProfileResponse profileFromCache = getProfileFromCache(ID.getConstant());
        assertNotNull(profileFromCache);
        assertEquals(profile, profileFromCache);
    }

    @Test
    public void doNotCacheProfileTest() {
        createStubForProfile(ID.getConstant(), 1000, 100);

        profileService.getProfile(ID.getConstant());
        profileService.getProfile(ID.getConstant());

        verify(profileRepository, times(2)).findById(ID.getConstant());

        ProfileResponse profileFromCache = getProfileFromCache(ID.getConstant());
        assertNull(profileFromCache);
    }

    @Test
    public void updateProfileInCacheTest() {
        createStubForProfile(ID.getConstant(), 10000, 1000);

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

        List<ProfileResponse> followers = getProfilesFromCache(ID.getConstant(), "followers");
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

        List<ProfileResponse> followees = getProfilesFromCache(ID.getConstant(), "followees");
        assertNotNull(followees);
        assertEquals(1000, followees.size());
    }

    @Test
    public void cacheFolloweesCelebritiesTest() {
        createStubForProfileWithFollowersAndFollowees(ID.getConstant(), 10, 100, 50);

        followService.getFolloweesCelebrities(ID.getConstant());
        followService.getFolloweesCelebrities(ID.getConstant());

        List<ProfileResponse> followeesCelebrities = getProfilesFromCache(ID.getConstant(), "followees_celebrities");
        assertNotNull(followeesCelebrities);
        assertEquals(50, followeesCelebrities.size());
    }

    private Profile createStubForProfileWithFollowersAndFollowees(String profileId, int followers, int followees, int followeesCelebrities) {
        Profile parentProfile = createStubForProfile(profileId, followers, followees);
        createStubForFollowersList(followers, parentProfile);
        createStubForFolloweesList(followees, followeesCelebrities, parentProfile);
        return parentProfile;
    }

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

    private Profile createStubForProfile(String profileId, int followers, int followees) {
        Profile profile = buildDefaultProfile(profileId);

        when(profileRepository.findById(profileId))
                .thenReturn(Optional.of(profile));

        when(profileRepository.save(profile))
                .thenReturn(profile);

        when(followsUtil.countFollowersForProfile(profileId))
                .thenReturn(followers);

        when(followsUtil.countFolloweesForProfile(profileId))
                .thenReturn(followees);

        return profile;
    }

    private Follow createStubForFollow(String id, Profile followerProfile, Profile followeeProfile) {
        return Follow.builder()
                .id(id)
                .followerProfile(followerProfile)
                .followeeProfile(followeeProfile)
                .followDateTime(LocalDateTime.now())
                .build();
    }

    @Nullable
    private ProfileResponse getProfileFromCache(String profileId) {
        ProfileResponse profile = cacheManager.getCache("profiles").get(profileId, ProfileResponse.class);
        return profile;
    }

    @Nullable
    private List<ProfileResponse> getProfilesFromCache(String parentProfileId, String cacheName) {
        List<ProfileResponse> followers = cacheManager.getCache(cacheName).get(parentProfileId, List.class);
        return followers;
    }

    @NonNull
    private UpdateProfileRequest buildUpdateProfileRequest() {
        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest(
                "new username",
                "new bio",
                "new location",
                "new website",
                LocalDate.of(2000, 01, 01)
        );
        return updateProfileRequest;
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
