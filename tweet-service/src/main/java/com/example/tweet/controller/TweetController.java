package com.example.tweet.controller;

import com.example.tweet.dto.request.TweetCreateRequest;
import com.example.tweet.dto.request.TweetUpdateRequest;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.service.TweetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tweets")
public class TweetController {

    private final TweetService tweetService;

    @PostMapping
    public ResponseEntity<TweetResponse> createTweet(
            @Valid @RequestBody TweetCreateRequest request,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(tweetService.createTweet(request, loggedInUser));
    }

    @PostMapping("/{tweetId}")
    public ResponseEntity<TweetResponse> createQuoteTweet(
            @PathVariable Long tweetId,
            @Valid @RequestBody TweetCreateRequest request,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(tweetService.createQuoteTweet(request, tweetId, loggedInUser));
    }

    @GetMapping("/{tweetId}")
    public ResponseEntity<TweetResponse> getTweet(@PathVariable Long tweetId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(tweetService.getTweet(tweetId, loggedInUser));
    }

    @GetMapping
    public ResponseEntity<List<TweetResponse>> getAllTweets() {
        return ResponseEntity.ok(tweetService.getAllTweets());
    }

    @PatchMapping("/{tweetId}")
    public ResponseEntity<TweetResponse> updateTweet(
            @Valid @RequestBody TweetUpdateRequest request,
            @PathVariable Long tweetId,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(tweetService.updateTweet(tweetId, request, loggedInUser));
    }

    @DeleteMapping("/{tweetId}")
    public ResponseEntity<Boolean> deleteTweet(
            @PathVariable Long tweetId,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(tweetService.deleteTweet(tweetId, loggedInUser));
    }
}
