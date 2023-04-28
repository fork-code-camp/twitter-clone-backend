package com.example.profile.dto.response;

import lombok.Builder;

@Builder
public record ErrorResponse(
        Integer code,
        String message,
        Long timestamp
) {

}
