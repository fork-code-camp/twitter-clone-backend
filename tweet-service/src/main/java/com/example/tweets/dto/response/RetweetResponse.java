package com.example.tweets.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetweetResponse {

    private TweetResponse tweet;
    private String username;
    private LocalDateTime retweetTime;
}
