package com.example.timeline.integration.controller;

import com.example.timeline.client.ProfileServiceClient;
import com.example.timeline.client.TweetServiceClient;
import com.example.timeline.constants.EntityName;
import com.example.timeline.dto.response.ProfileResponse;
import com.example.timeline.dto.response.TweetResponse;
import com.example.timeline.integration.IntegrationTestBase;
import com.example.timeline.integration.constants.TimelineCachePrefix;
import com.example.timeline.service.CacheService;
import com.example.timeline.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static com.example.timeline.integration.constants.UrlConstants.*;
import static com.example.timeline.integration.constants.TimelineCachePrefix.*;
import static com.example.timeline.constants.EntityName.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.testcontainers.utility.Base58.randomString;

@AutoConfigureMockMvc
@RequiredArgsConstructor
@SuppressWarnings("all")
public class TimelineControllerTest extends IntegrationTestBase {

    private final MockMvc mockMvc;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TimelineService timelineService;
    private final CacheService cacheService;

    @MockBean
    private final TweetServiceClient tweetServiceClient;

    @MockBean
    private final ProfileServiceClient profileServiceClient;

    @Test
    public void getUserTimelineTest() throws Exception {
        ProfileResponse profile = buildDefaultProfile("id", "email");
        buildTweetsTimeline(10, profile);
        buildRetweetsTimeline(10, profile);
        buildRepliesTimeline(10, profile);

        getTimelineAndExpectSuccess(profile, USER_TIMELINE_URL.getConstant(), 0, 20, 20);
        getTimelineAndExpectSuccess(profile, USER_TIMELINE_URL.getConstant(), 0, 10, 10);
        getTimelineAndExpectSuccess(profile, USER_TIMELINE_URL.getConstant(), 0, 50, 20);
        getTimelineAndExpectSuccess(profile, USER_TIMELINE_URL_FOR_USER.getConstant().formatted(profile.getProfileId()), 0, 20, 20);

        getTimelinesFromCacheAndExpectSuccess(USER_TIMELINE_PREFIX, TWEETS,10, RETWEETS, 10, profile);
    }

    @Test
    public void getUserRepliesTimelineTest() throws Exception {
        ProfileResponse profile = buildDefaultProfile("id", "email");
        buildRepliesTimeline(10, profile);
        buildRetweetsTimeline(10, profile);
        buildTweetsTimeline(10, profile);

        getTimelineAndExpectSuccess(profile, USER_REPLIES_TIMELINE_URL.getConstant(), 0, 20, 20);
        getTimelineAndExpectSuccess(profile, USER_REPLIES_TIMELINE_URL.getConstant(), 0, 10, 10);
        getTimelineAndExpectSuccess(profile, USER_REPLIES_TIMELINE_URL.getConstant(), 0, 50, 20);
        getTimelineAndExpectSuccess(profile, USER_REPLIES_TIMELINE_URL_FOR_USER.getConstant().formatted(profile.getProfileId()), 0, 20, 20);

        getTimelinesFromCacheAndExpectSuccess(USER_TIMELINE_PREFIX, REPLIES,10, RETWEETS, 10, profile);
    }

    @Test
    public void getHomeTimelineTest() throws Exception {
        ProfileResponse follower = buildDefaultProfile("follower id", "follower email");
        List<ProfileResponse> followees = buildFolloweesForProfile(follower, 10, 1);
        List<ProfileResponse> followeesCelebrities = buildFolloweesForProfile(follower, 5, 10000);

        for (ProfileResponse followee : followees) {
            buildTweetsTimeline(5, followee);
            buildRetweetsTimeline(5, followee);
            buildRepliesTimeline(5, followee);
        }
        for (ProfileResponse followeeCelebrity : followeesCelebrities) {
            buildTweetsTimeline(5, followeeCelebrity);
            buildRetweetsTimeline(5, followeeCelebrity);
            buildRepliesTimeline(5, followeeCelebrity);
        }

        getTimelineAndExpectSuccess(follower, HOME_TIMELINE_URL.getConstant(), 0, 20, 70);
        getTimelineAndExpectSuccess(follower, HOME_TIMELINE_URL.getConstant(), 0, 100, 150);
        getTimelineAndExpectSuccess(follower, HOME_TIMELINE_URL.getConstant(), 0, 200, 150);

        getTimelinesFromCacheAndExpectSuccess(HOME_TIMELINE_PREFIX, TWEETS, 50, RETWEETS, 50, follower);
    }

    private void getTimelineAndExpectSuccess(ProfileResponse profile, String url, int page, int size, int numberOfEntities) throws Exception {
        mockMvc.perform(get(
                        url)
                        .header("loggedInUser", profile.getEmail())
                        .param("page", Integer.toString(page))
                        .param("size", Integer.toString(size))
                )
                .andExpectAll(
                        jsonPath("$.length()").value(numberOfEntities)
                );
    }

    private void getTimelinesFromCacheAndExpectSuccess(
            TimelineCachePrefix cachePrefix,
            EntityName entityName1,
            int timelineSize1,
            EntityName entityName2,
            int timelineSize2,
            ProfileResponse profile
    ) {
        List<TweetResponse> timeline1 = cacheService.getTimelineFromCache(
                cachePrefix.getPrefix().formatted(entityName1.getName()) + profile.getProfileId()
        );
        List<TweetResponse> timeline2 = cacheService.getTimelineFromCache(
                cachePrefix.getPrefix().formatted(entityName2.getName()) + profile.getProfileId()
        );

        assertNotNull(timeline1);
        assertEquals(timelineSize1, timeline1.size());
        assertNotNull(timeline2);
        assertEquals(timelineSize2, timeline2.size());
    }

    private List<ProfileResponse> buildFolloweesForProfile(ProfileResponse profile, int followees, int followersForFollowee) {
        List<ProfileResponse> followeeList = new LinkedList<>();
        for (int i = 0; i < followees; i++) {
            ProfileResponse followee = buildDefaultProfile(randomString(10), randomString(10));
            followee.setFollowers(followersForFollowee);
            followeeList.add(followee);

            when(profileServiceClient.getProfileIdByLoggedInUser(followee.getEmail()))
                    .thenReturn(followee.getProfileId());

            when(profileServiceClient.getProfileById(followee.getProfileId()))
                    .thenReturn(followee);
        }

        if (followersForFollowee < 10000) {
            when(profileServiceClient.getFollowees(profile.getProfileId()))
                    .thenReturn(followeeList);
        } else {
            when(profileServiceClient.getFolloweesCelebrities(profile.getProfileId()))
                    .thenReturn(followeeList);
        }

        return followeeList;
    }

    private void buildRepliesTimeline(int replies, ProfileResponse profile) {
        List<TweetResponse> repliesForUser = new LinkedList<>();
        for (int i = 0; i < replies; i++) {
            repliesForUser.add(buildDefaultTweet(RandomUtils.nextLong()));
        }

        when(tweetServiceClient.getAllRepliesForUser(eq(profile.getProfileId()), anyInt(), anyInt()))
                .thenReturn(repliesForUser);
    }

    private void buildRetweetsTimeline(int retweets, ProfileResponse profile) {
        List<TweetResponse> retweetsForUser = new LinkedList<>();
        for (int i = 0; i < retweets; i++) {
            retweetsForUser.add(buildDefaultTweet(RandomUtils.nextLong()));
        }

        when(tweetServiceClient.getAllRetweetsForUser(eq(profile.getProfileId()), anyInt(), anyInt()))
                .thenReturn(retweetsForUser);
    }

    private void buildTweetsTimeline(int tweets, ProfileResponse profile) {
        List<TweetResponse> tweetsForUser = new LinkedList<>();
        for (int i = 0; i < tweets; i++) {
            tweetsForUser.add(buildDefaultTweet(RandomUtils.nextLong()));
        }

        when(tweetServiceClient.getAllTweetsForUser(eq(profile.getProfileId()), anyInt(), anyInt()))
                .thenReturn(tweetsForUser);
    }

    private ProfileResponse buildDefaultProfile(String id, String email) {
        ProfileResponse profile = ProfileResponse.builder()
                .profileId(id)
                .email(email)
                .build();

        when(profileServiceClient.getProfileById(id))
                .thenReturn(profile);

        when(profileServiceClient.getProfileIdByLoggedInUser(email))
                .thenReturn(id);

        return profile;
    }

    private TweetResponse buildDefaultReply(Long id, TweetResponse replyTo) {
        return TweetResponse.builder()
                .id(id)
                .text(randomString(10))
                .replyTo(replyTo)
                .creationDate(LocalDateTime.now())
                .build();
    }

    private TweetResponse buildDefaultRetweet(Long id, TweetResponse retweetTo) {
        return TweetResponse.builder()
                .id(id)
                .text(randomString(10))
                .retweetTo(retweetTo)
                .creationDate(LocalDateTime.now())
                .build();
    }

    private TweetResponse buildDefaultTweet(Long id) {
        return TweetResponse.builder()
                .id(id)
                .text(randomString(10))
                .creationDate(LocalDateTime.now())
                .build();
    }
}
