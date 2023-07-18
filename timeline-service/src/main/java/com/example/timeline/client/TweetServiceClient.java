package com.example.timeline.client;

import com.example.timeline.dto.response.TweetResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("${services.tweet.name}")
public interface TweetServiceClient {

    @GetMapping("/api/v1/tweets/user/{profileId}")
    List<TweetResponse> getAllTweetsForUser(
            @PathVariable String profileId,
            @RequestParam int page,
            @RequestParam int size
    );

    @GetMapping("/api/v1/retweets/user/{profileId}")
    List<TweetResponse> getAllRetweetsForUser(
            @PathVariable String profileId,
            @RequestParam int page,
            @RequestParam int size
    );

    @GetMapping("/api/v1/replies/user/{profileId}")
    List<TweetResponse> getAllRepliesForUser(
            @PathVariable String profileId,
            @RequestParam int page,
            @RequestParam int size
    );

    @GetMapping("/api/v1/tweet/{tweetId}")
    TweetResponse getTweet(@PathVariable Long tweetId, @RequestHeader String loggedInUser);

    @GetMapping("/api/v1/retweet/{retweetId}")
    TweetResponse getRetweet(@PathVariable Long retweetId, @RequestHeader String loggedInUser);

    @GetMapping("/api/v1/reply/{replyId}")
    TweetResponse getReply(@PathVariable Long replyId, @RequestHeader String loggedInUser);
}
