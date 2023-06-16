package com.example.tweet.integration.controller;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.client.StorageServiceClient;
import com.example.tweet.dto.response.ProfileResponse;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.entity.Tweet;
import com.example.tweet.integration.IntegrationTestBase;
import com.example.tweet.service.CacheService;
import com.example.tweet.service.FanoutService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.when;
import static com.example.tweet.service.FanoutService.EntityName.*;
import static com.example.tweet.service.FanoutService.TimelineCachePrefix.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.utility.Base58.randomString;

@AutoConfigureMockMvc
@RequiredArgsConstructor
@SuppressWarnings("all")
public class FanoutServiceTest extends IntegrationTestBase {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheService cacheService;
    private final FanoutService fanoutService;

    @MockBean
    private final ProfileServiceClient profileServiceClient;

    @MockBean
    private final StorageServiceClient storageServiceClient;

    @Test
    public void userTimelineTest() {
        ProfileResponse profile = buildDefaultProfile("id", "email");
        Tweet tweet = buildDefaultTweet(RandomUtils.nextLong(), profile);
        TweetResponse tweetResponse = buildTweetResponse(tweet, profile);

        String timelineKey = USER_TIMELINE_PREFIX.getPrefix().formatted(TWEETS.getName()) + profile.getProfileId();
        cacheService.cacheTimeline(new LinkedList<>(), timelineKey);

        fanoutService.addToUserTimeline(tweetResponse, TWEETS);
        validateTimelineFromCache(tweetResponse, cacheService.getTimelineFromCache(timelineKey), 1);

        tweetResponse.setText(randomString(5));
        fanoutService.updateInUserTimeline(tweetResponse, TWEETS);
        validateTimelineFromCache(tweetResponse, cacheService.getTimelineFromCache(timelineKey), 1);

        fanoutService.deleteFromUserTimeline(tweet, TWEETS);
        validateTimelineFromCache(null, cacheService.getTimelineFromCache(timelineKey), 0);
    }

    @Test
    public void homeTimelineTest() {
        ProfileResponse followee = buildDefaultProfile("followee id", "followee email");
        List<ProfileResponse> followers = buildFollowersForProfile(followee, 3);
        Tweet tweet = buildDefaultTweet(RandomUtils.nextLong(), followee);
        TweetResponse tweetResponse = buildTweetResponse(tweet, followee);

        fanoutService.addToHomeTimelines(tweetResponse, TWEETS);
        validateHomeTimelines(followers, tweetResponse, 1);

        tweetResponse.setText(randomString(5));
        fanoutService.updateInHomeTimelines(tweetResponse, TWEETS);
        validateHomeTimelines(followers, tweetResponse, 1);

        fanoutService.deleteFromHomeTimelines(tweet, TWEETS);
        validateHomeTimelines(followers, null, 0);
    }

    private static void validateTimelineFromCache(TweetResponse tweetResponse, List<TweetResponse> tweetsFromCache, int timelineSize) {
        assertNotNull(tweetsFromCache);
        assertEquals(timelineSize, tweetsFromCache.size());
        if (tweetResponse != null) {
            assertEquals(tweetResponse, tweetsFromCache.get(0));
        }
    }

    private void validateHomeTimelines(List<ProfileResponse> followers, TweetResponse tweetResponse, int timelineSize) {
        for (ProfileResponse follower : followers) {
            String timelineKey = HOME_TIMELINE_PREFIX.getPrefix().formatted(TWEETS.getName()) + follower.getProfileId();
            validateTimelineFromCache(tweetResponse, cacheService.getTimelineFromCache(timelineKey), timelineSize);
        }
    }

    private TweetResponse buildTweetResponse(Tweet tweet, ProfileResponse profile) {
        return TweetResponse.builder()
                .id(tweet.getId())
                .text(tweet.getText())
                .profile(profile)
                .creationDate(tweet.getCreationDate())
                .build();
    }

    private Tweet buildDefaultTweet(Long id, ProfileResponse profile) {
        return Tweet.builder()
                .id(id)
                .profileId(profile.getProfileId())
                .text(randomString(10))
                .creationDate(LocalDateTime.now())
                .build();
    }

    private List<ProfileResponse> buildFollowersForProfile(ProfileResponse profile, int followers) {
        List<ProfileResponse> followerList = new LinkedList<>();
        for (int i = 0; i < followers; i++) {
            ProfileResponse follower = buildDefaultProfile(randomString(5), randomString(5));
            cacheService.cacheTimeline(new LinkedList<>(), HOME_TIMELINE_PREFIX.getPrefix().formatted(TWEETS.getName()) + follower.getProfileId());
            followerList.add(follower);
        }

        when(profileServiceClient.getFollowers(profile.getProfileId()))
                .thenReturn(followerList);

        return followerList;
    }

    private ProfileResponse buildDefaultProfile(String id, String email) {
        ProfileResponse profile = ProfileResponse.builder()
                .profileId(id)
                .email(email)
                .build();

        when(profileServiceClient.getProfileById(profile.getProfileId()))
                .thenReturn(profile);

        when(profileServiceClient.getProfileIdByLoggedInUser(profile.getEmail()))
                .thenReturn(profile.getProfileId());

        return profile;
    }
}
