package com.example.authentication.dto.response;

import lombok.Builder;

@Builder
public record ErrorResponse(
        Integer code,
        String message,
        Long timestamp
) {

}
