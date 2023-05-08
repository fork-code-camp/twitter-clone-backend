package com.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TweetCreateRequest(
        @NotNull(message = "{text.not-null}")
        @NotEmpty(message = "{text.not-empty}")
        @NotBlank(message = "{text.not-empty}")
        String text
) {
}
