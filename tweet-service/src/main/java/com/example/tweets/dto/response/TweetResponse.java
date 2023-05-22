package com.example.tweets.dto.response;

import com.example.tweets.client.response.ProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TweetResponse {

    private TweetResponse replyTo;
    private ProfileResponse profile;
    private String text;
    private TweetResponse quoteTo;
    private Integer retweets;
    private Integer replies;
    private Integer likes;
    private Integer views;
    private LocalDateTime creationDate;
}
