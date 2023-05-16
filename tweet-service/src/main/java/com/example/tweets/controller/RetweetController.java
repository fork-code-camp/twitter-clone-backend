package com.example.tweets.controller;

import com.example.tweets.dto.request.RetweetRequest;
import com.example.tweets.dto.response.TweetResponse;
import com.example.tweets.service.RetweetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/retweets")
@RequiredArgsConstructor
public class RetweetController {

    private final RetweetService retweetService;

    @PostMapping("/{tweetId}")
    public ResponseEntity<Boolean> retweet(
            @RequestBody RetweetRequest retweetRequest,
            @PathVariable Long tweetId,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(retweetService.retweet(retweetRequest, tweetId, loggedInUser));
    }

    @DeleteMapping("/{tweetId}")
    public ResponseEntity<Boolean> undoRetweet(@PathVariable Long tweetId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(retweetService.undoRetweet(tweetId, loggedInUser));
    }

    @GetMapping("/{tweetId}")
    public ResponseEntity<TweetResponse> getRetweetByTweetId(@PathVariable Long tweetId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(retweetService.getRetweetByOriginalTweetId(tweetId, loggedInUser));
    }

    @GetMapping
    public ResponseEntity<List<TweetResponse>> getRetweetsForUser(@RequestHeader String loggedInUser) {
        return ResponseEntity.ok(retweetService.getRetweetsForUser(loggedInUser));
    }
}
