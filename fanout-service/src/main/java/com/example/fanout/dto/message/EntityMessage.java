package com.example.fanout.dto.message;

import lombok.Builder;

@Builder
public record EntityMessage (
        Long entityId,
        String profileId,
        String entityName,
        String operation
) {
}
