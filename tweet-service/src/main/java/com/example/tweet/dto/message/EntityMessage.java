package com.example.tweet.dto.message;

import com.example.tweet.constant.EntityName;
import com.example.tweet.constant.Operation;
import lombok.Builder;

@Builder
public record EntityMessage (
        Long entityId,
        String profileId,
        String entityName,
        String operation
) {
    public static EntityMessage valueOf(Long entityId, String profileId, EntityName entityName, Operation operation) {
        return EntityMessage.builder()
                .entityId(entityId)
                .profileId(profileId)
                .entityName(entityName.getName())
                .operation(operation.getOperation())
                .build();
    }
}
