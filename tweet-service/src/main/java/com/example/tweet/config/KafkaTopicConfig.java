package com.example.tweet.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

import static com.example.tweet.constant.TopicName.HOME_TIMELINE_TOPIC;
import static com.example.tweet.constant.TopicName.USER_TIMELINE_TOPIC;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public KafkaAdmin.NewTopics topics() {
        return new KafkaAdmin.NewTopics(
                new NewTopic(USER_TIMELINE_TOPIC, 1, (short) 1),
                new NewTopic(HOME_TIMELINE_TOPIC, 1, (short) 1)
        );
    }
}
