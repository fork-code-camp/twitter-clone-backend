package com.example.tweet.integration;

import com.example.tweet.integration.testcomponents.KafkaTestConsumer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@ActiveProfiles("test")
public class TestApplicationRunner {

    @Bean
    public KafkaTestConsumer kafkaTestConsumer() {
        return new KafkaTestConsumer();
    }
}
