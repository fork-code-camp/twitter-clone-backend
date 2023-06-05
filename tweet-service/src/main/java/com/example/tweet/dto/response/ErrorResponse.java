package com.example.tweet.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
        String message,
        Integer code,
        LocalDateTime timestamp
) {
}
