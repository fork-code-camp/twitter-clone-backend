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

    private ProfileResponse profile;
    private TweetResponse originalTweet;
    private String text;
    private Integer likes;
    private Integer retweets;
    private LocalDateTime creationDate;
}
