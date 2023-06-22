package com.example.tweet.controller;

import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.service.RetweetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    public ResponseEntity<Boolean> undoRetweet(@PathVariable Long tweetId) {
        return ResponseEntity.ok(retweetService.undoRetweet(tweetId));
    }

    @GetMapping("/{retweetId}")
    public ResponseEntity<TweetResponse> getRetweet(@PathVariable Long retweetId) {
        return ResponseEntity.ok(retweetService.getRetweetById(retweetId));
    }

    @GetMapping("/user/{profileId}")
    public ResponseEntity<List<TweetResponse>> getAllRetweetsForUser(
            @PathVariable String profileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(retweetService.getAllRetweetsForUser(profileId, PageRequest.of(page, size)));
    }
}
