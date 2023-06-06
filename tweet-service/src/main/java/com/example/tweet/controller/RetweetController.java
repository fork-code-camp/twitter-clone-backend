package com.example.tweet.controller;

import com.example.tweet.dto.response.RetweetResponse;
import com.example.tweet.service.RetweetService;
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
    public ResponseEntity<Boolean> retweet(@PathVariable Long tweetId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(retweetService.retweet(tweetId, loggedInUser));
    }

    @DeleteMapping("/{tweetId}")
    public ResponseEntity<Boolean> undoRetweet(@PathVariable Long tweetId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(retweetService.undoRetweet(tweetId, loggedInUser));
    }

    @GetMapping("/{retweetId}")
    public ResponseEntity<RetweetResponse> getRetweet(@PathVariable Long retweetId) {
        return ResponseEntity.ok(retweetService.findRetweetById(retweetId));
    }

    @GetMapping
    public ResponseEntity<List<RetweetResponse>> getRetweetsForUser(@RequestHeader String loggedInUser) {
        return ResponseEntity.ok(retweetService.findRetweetsForUser(loggedInUser));
    }
}
