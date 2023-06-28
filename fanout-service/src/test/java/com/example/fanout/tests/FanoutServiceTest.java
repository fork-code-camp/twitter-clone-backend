package com.example.fanout.tests;

import com.example.fanout.IntegrationTestBase;
import com.example.fanout.client.ProfileServiceClient;
import com.example.fanout.constants.EntityName;
import com.example.fanout.constants.Operation;
import com.example.fanout.dto.message.Message;
import com.example.fanout.dto.response.ProfileResponse;
import com.example.fanout.dto.response.TweetResponse;
import com.example.fanout.service.CacheService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.testcontainers.shaded.com.google.common.reflect.TypeToken;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static com.example.fanout.constants.EntityName.TWEETS;
import static com.example.fanout.constants.Operation.*;
import static com.example.fanout.constants.TimelineCachePrefix.HOME_TIMELINE_PREFIX;
import static com.example.fanout.constants.TimelineCachePrefix.USER_TIMELINE_PREFIX;
import static com.example.fanout.constants.TopicName.HOME_TIMELINE_TOPIC;
import static com.example.fanout.constants.TopicName.USER_TIMELINE_TOPIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.testcontainers.utility.Base58.randomString;

@RequiredArgsConstructor
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" },
        topics = { USER_TIMELINE_TOPIC, HOME_TIMELINE_TOPIC }
)
@SuppressWarnings("all")
public class FanoutServiceTest extends IntegrationTestBase {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Gson gson;
    private final CacheService cacheService;

    @MockBean
    private final ProfileServiceClient profileServiceClient;

    @Test
    public void userTimelineTest() {
        ProfileResponse profile = buildDefaultProfile("id", "email");
        TweetResponse tweet = buildDefaultTweet(RandomUtils.nextLong(), profile);

        String timelineKey = USER_TIMELINE_PREFIX.getPrefix().formatted(TWEETS.getName()) + profile.getProfileId();
        cacheService.cacheTimeline(new LinkedList<>(), timelineKey);

        sendMessageToKafka(USER_TIMELINE_TOPIC, buildDefaultMessage(tweet, TWEETS, ADD));
        validateTimelineFromCache(tweet, cacheService.getTimelineFromCache(timelineKey), 1);

        tweet.setText(randomString(5));
        tweet.setLikes(RandomUtils.nextInt());
        tweet.setViews(RandomUtils.nextInt());
        tweet.setReplies(RandomUtils.nextInt());
        sendMessageToKafka(USER_TIMELINE_TOPIC, buildDefaultMessage(tweet, TWEETS, UPDATE));
        validateTimelineFromCache(tweet, cacheService.getTimelineFromCache(timelineKey), 1);

        sendMessageToKafka(USER_TIMELINE_TOPIC, buildDefaultMessage(tweet, TWEETS, DELETE));
        validateTimelineFromCache(null, cacheService.getTimelineFromCache(timelineKey), 0);
    }

    @Test
    public void homeTimelineTest() {
        ProfileResponse followee = buildDefaultProfile("followee id", "followee email");
        List<ProfileResponse> followers = buildFollowersForProfile(followee, 3);
        TweetResponse tweet = buildDefaultTweet(RandomUtils.nextLong(), followee);

        sendMessageToKafka(HOME_TIMELINE_TOPIC, buildDefaultMessage(tweet, TWEETS, ADD));
        validateHomeTimelines(followers, tweet, 1);

        tweet.setText(randomString(5));
        tweet.setLikes(RandomUtils.nextInt());
        tweet.setViews(RandomUtils.nextInt());
        tweet.setReplies(RandomUtils.nextInt());
        sendMessageToKafka(HOME_TIMELINE_TOPIC, buildDefaultMessage(tweet, TWEETS, UPDATE));
        validateHomeTimelines(followers, tweet, 1);

        sendMessageToKafka(HOME_TIMELINE_TOPIC, buildDefaultMessage(tweet, TWEETS, DELETE));
        validateHomeTimelines(followers, null, 0);
    }

    private void validateHomeTimelines(List<ProfileResponse> followers, TweetResponse tweetResponse, int timelineSize) {
        for (ProfileResponse follower : followers) {
            String timelineKey = HOME_TIMELINE_PREFIX.getPrefix().formatted(TWEETS.getName()) + follower.getProfileId();
            validateTimelineFromCache(tweetResponse, cacheService.getTimelineFromCache(timelineKey), timelineSize);
        }
    }

    private static void validateTimelineFromCache(TweetResponse tweetResponse, List<TweetResponse> tweetsFromCache, int timelineSize) {
        assertNotNull(tweetsFromCache);
        assertEquals(timelineSize, tweetsFromCache.size());
        if (tweetResponse != null) {
            assertEquals(tweetResponse, tweetsFromCache.get(0));
        }
    }

    @SneakyThrows
    private void sendMessageToKafka(String topic, Message<TweetResponse> message) {
        String msg = gson.toJson(message, new TypeToken<Message<TweetResponse>>(){}.getType());
        kafkaTemplate.send(topic, msg);
        Thread.sleep(500);
    }

    private Message<TweetResponse> buildDefaultMessage(TweetResponse entity, EntityName entityName, Operation operation) {
        Message.MessageBuilder<TweetResponse> messageBuilder = Message.builder();
        return messageBuilder
                .entity(entity)
                .entityName(entityName.getName())
                .operation(operation.getOperation())
                .build();
    }

    private TweetResponse buildDefaultTweet(Long id, ProfileResponse profile) {
        return TweetResponse.builder()
                .id(id)
                .text(randomString(10))
                .views(RandomUtils.nextInt())
                .likes(RandomUtils.nextInt())
                .replies(RandomUtils.nextInt())
                .retweets(RandomUtils.nextInt())
                .isLiked(RandomUtils.nextBoolean())
                .isRetweeted(RandomUtils.nextBoolean())
                .profile(profile)
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
