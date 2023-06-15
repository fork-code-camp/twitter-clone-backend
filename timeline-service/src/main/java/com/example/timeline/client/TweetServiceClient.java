package com.example.timeline.client;

import com.example.timeline.dto.response.TweetResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("${services.tweet.name}")
public interface TweetServiceClient {

    @GetMapping("/api/v1/tweets")
    List<TweetResponse> getAllTweetsForUser(
            @RequestHeader String loggedInUser,
            @RequestParam int page,
            @RequestParam int size
    );

    @GetMapping("/api/v1/retweets")
    List<TweetResponse> getAllRetweetsForUser(
            @RequestHeader String loggedInUser,
            @RequestParam int page,
            @RequestParam int size
    );

    @GetMapping("/api/v1/replies")
    List<TweetResponse> getAllRepliesForUser(
            @RequestHeader String loggedInUser,
            @RequestParam int page,
            @RequestParam int size
    );
}
