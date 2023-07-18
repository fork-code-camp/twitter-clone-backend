package com.example.tweet.integration.tests;


import com.example.tweet.client.StorageServiceClient;
import com.example.tweet.dto.message.EntityMessage;
import com.example.tweet.dto.response.ProfileResponse;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.integration.IntegrationTestBase;
import com.example.tweet.util.TweetUtil;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.example.tweet.constant.EntityName.TWEETS;
import static com.example.tweet.constant.Operation.ADD;
import static com.example.tweet.constant.TopicName.USER_TIMELINE_TOPIC;
import static com.example.tweet.integration.constants.GlobalConstants.EMAIL;
import static com.example.tweet.integration.constants.GlobalConstants.USERNAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.utility.Base58.randomString;

@RequiredArgsConstructor
@SuppressWarnings("all")
public class KafkaTest extends IntegrationTestBase {

    private final TweetUtil tweetUtil;
    private final Gson gson;
    private final KafkaTestConsumer kafkaConsumer;

    @MockBean
    private final StorageServiceClient storageServiceClient;

    @Test
    @SneakyThrows
    public void kafkaSendMessageTest() {
        TweetResponse tweet = buildDefaultTweet(RandomUtils.nextLong());
        tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC.toString(), tweet, TWEETS, ADD);

        boolean messageReceived = kafkaConsumer.getLatch().await(10, TimeUnit.SECONDS);
        EntityMessage receivedEntityMessage = gson.fromJson(kafkaConsumer.getPayload(), EntityMessage.class);

        assertTrue(messageReceived);
        assertNotNull(receivedEntityMessage);
        assertEquals(tweet.getId(), receivedEntityMessage.entityId());
        assertEquals(tweet.getProfile().getProfileId(), receivedEntityMessage.profileId());
    }

    private TweetResponse buildDefaultTweet(Long id) {
        return TweetResponse.builder()
                .id(id)
                .profile(buildDefaultProfile(randomString(15)))
                .text(randomString(10))
                .creationDate(LocalDateTime.now())
                .build();
    }

    private ProfileResponse buildDefaultProfile(String id) {
        return ProfileResponse.builder()
                .profileId(id)
                .email(EMAIL.getConstant())
                .username(USERNAME.getConstant())
                .build();
    }
}
