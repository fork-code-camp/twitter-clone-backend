package com.example.controller;

import com.example.dto.request.TweetCreateRequest;
import com.example.dto.request.TweetUpdateRequest;
import com.example.dto.response.TweetResponse;
import com.example.service.TweetService;
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
            @RequestBody TweetCreateRequest request,
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
            @RequestBody TweetUpdateRequest request,
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
