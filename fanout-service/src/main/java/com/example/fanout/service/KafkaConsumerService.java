package com.example.fanout.service;

import com.example.fanout.dto.message.EntityMessage;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.example.fanout.constants.TopicName.HOME_TIMELINE_TOPIC;
import static com.example.fanout.constants.TopicName.USER_TIMELINE_TOPIC;


@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final FanoutService fanoutService;
    private final Gson gson;

    @Async
    @KafkaListener(topics = USER_TIMELINE_TOPIC)
    public CompletableFuture<Void> receiveEntityMessageForUserTimeline(String msg) {
        log.info("message for {} has been successfully received {}", USER_TIMELINE_TOPIC, msg);
        EntityMessage entityMessage = gson.fromJson(msg, EntityMessage.class);
        fanoutService.processMessageForUserTimeline(entityMessage);
        return CompletableFuture.completedFuture(null);
    }

    @Async
    @KafkaListener(topics = HOME_TIMELINE_TOPIC)
    public CompletableFuture<Void> receiveEntityMessageForHomeTimeline(String msg) {
        log.info("message for {} has been successfully received {}", HOME_TIMELINE_TOPIC, msg);
        EntityMessage entityMessage = gson.fromJson(msg, EntityMessage.class);
        fanoutService.processMessageForHomeTimeline(entityMessage);
        return CompletableFuture.completedFuture(null);
    }
}
