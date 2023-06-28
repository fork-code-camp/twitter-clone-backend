package com.example.tweet.producers;

import com.example.tweet.dto.message.Message;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Gson gson;

    public <T> void send(Message<T> message, String topic, Type type) {
        String msg = gson.toJson(message, type);
        kafkaTemplate.send(topic, msg);
    }
}
