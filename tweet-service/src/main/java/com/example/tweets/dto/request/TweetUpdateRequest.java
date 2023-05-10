package com.example.tweets.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TweetUpdateRequest(
        @NotEmpty(message = "{text.not_empty}")
        @NotBlank(message = "{text.not_empty}")
        String text
) {
}
