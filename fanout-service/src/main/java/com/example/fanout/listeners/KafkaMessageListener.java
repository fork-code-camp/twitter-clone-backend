package com.example.fanout.listeners;

import com.example.fanout.dto.message.Message;
import com.example.fanout.dto.response.TweetResponse;
import com.example.fanout.service.FanoutService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.testcontainers.shaded.com.google.common.reflect.TypeToken;

import static com.example.fanout.constants.TopicName.HOME_TIMELINE_TOPIC;
import static com.example.fanout.constants.TopicName.USER_TIMELINE_TOPIC;


@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageListener {

    private final FanoutService fanoutService;
    private final Gson gson;

    @KafkaListener(topics = USER_TIMELINE_TOPIC)
    public void userTimelineListener(String msg) {
        log.info("message for user timeline has been successfully received {}", msg);
        Message<TweetResponse> message = gson.fromJson(msg, new TypeToken<Message<TweetResponse>>(){}.getType());
        fanoutService.processMessageForUserTimeline(message);
    }

    @KafkaListener(topics = HOME_TIMELINE_TOPIC)
    public void homeTimelineListener(String msg) {
        log.info("message for home timeline has been successfully received {}", msg);
        Message<TweetResponse> message = gson.fromJson(msg, new TypeToken<Message<TweetResponse>>(){}.getType());
        fanoutService.processMessageForHomeTimeline(message);
    }
}
