package com.example.tweet.dto.message;

import com.example.tweet.constants.EntityName;
import com.example.tweet.constants.Operation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message <T> {

    private T entity;
    private String entityName;
    private String operation;

    public static <T> Message<T> of(T entity, String entityName, String operation) {
        MessageBuilder<T> messageBuilder = Message.builder();
        return messageBuilder
                .entity(entity)
                .entityName(entityName)
                .operation(operation)
                .build();
    }

    public static <T> Message<T> of(T entity, EntityName entityName, Operation operation) {
        MessageBuilder<T> messageBuilder = Message.builder();
        return messageBuilder
                .entity(entity)
                .entityName(entityName.getName())
                .operation(operation.getOperation())
                .build();
    }
}
