package com.example.tweet.integration.testcomponents;

import com.example.tweet.constants.TopicName;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.concurrent.CountDownLatch;

@Data
@TestComponent
@Slf4j
public class KafkaTestConsumer {

    private String payload;
    private CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = TopicName.USER_TIMELINE_TOPIC)
    public void receive(String msg) {
        log.info("received message {}", msg);
        payload = msg;
        latch.countDown();
    }

    public void resetLatch() {
        latch = new CountDownLatch(1);
    }
}

