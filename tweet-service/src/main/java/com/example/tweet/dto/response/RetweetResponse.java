package com.example.tweet.dto.response;

import com.example.tweet.client.response.ProfileResponse;
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

    private TweetResponse parentTweet;
    private ProfileResponse profile;
    private LocalDateTime retweetTime;
}
