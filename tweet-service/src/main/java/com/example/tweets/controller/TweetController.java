package com.example.tweets.controller;

import com.example.tweets.dto.request.TweetCreateRequest;
import com.example.tweets.dto.request.TweetUpdateRequest;
import com.example.tweets.dto.response.TweetResponse;
import com.example.tweets.service.TweetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    public ResponseEntity<TweetResponse> getTweet(@PathVariable Long id) {
        return ResponseEntity.ok(tweetService.getTweet(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TweetResponse> updateTweet(
            @Valid @RequestBody TweetUpdateRequest request,
            @PathVariable Long id,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(tweetService.updateTweet(id, request, loggedInUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteTweet(
            @PathVariable Long id,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(tweetService.deleteTweet(id, loggedInUser));
    }
}
