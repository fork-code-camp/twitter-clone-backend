package com.example.tweet.integration.tests;


import com.example.tweet.client.StorageServiceClient;
import com.example.tweet.dto.message.Message;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.integration.IntegrationTestBase;
import com.example.tweet.integration.testcomponents.KafkaTestConsumer;
import com.example.tweet.util.TweetUtil;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.shaded.com.google.common.reflect.TypeToken;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.example.tweet.constants.EntityName.TWEETS;
import static com.example.tweet.constants.Operation.ADD;
import static com.example.tweet.constants.TopicName.USER_TIMELINE_TOPIC;
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
        tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, tweet, TWEETS, ADD);

        boolean messageReceived = kafkaConsumer.getLatch().await(10, TimeUnit.SECONDS);
        Message<TweetResponse> receivedMessage = gson.fromJson(kafkaConsumer.getPayload(), new TypeToken<Message<TweetResponse>>(){}.getType());

        assertTrue(messageReceived);
        assertNotNull(receivedMessage);
        assertEquals(tweet, receivedMessage.getEntity());
    }

    private TweetResponse buildDefaultTweet(Long id) {
        return TweetResponse.builder()
                .id(id)
                .text(randomString(10))
                .creationDate(LocalDateTime.now())
                .build();
    }
}
