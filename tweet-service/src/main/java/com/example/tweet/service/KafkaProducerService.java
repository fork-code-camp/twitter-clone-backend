package com.example.tweet.service;

import com.example.tweet.dto.message.EntityMessage;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Gson gson;

    public void send(EntityMessage entityMessage, String topic) {
        String msg = gson.toJson(entityMessage);
        kafkaTemplate.send(topic, msg);
    }
}
