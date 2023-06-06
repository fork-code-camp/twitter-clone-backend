package com.example.storage.dto;

import lombok.Builder;

@Builder
public record ErrorResponse(
        Integer code,
        String message,
        Long timestamp
) {

}
