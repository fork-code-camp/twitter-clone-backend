package com.example.tweets.controller;

import com.example.tweets.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{tweetId}")
    public ResponseEntity<Void> likeTweet(@PathVariable Long tweetId, @RequestHeader String loggedInUser) {
        likeService.likeTweet(tweetId, loggedInUser);
        return ResponseEntity.status(CREATED).build();
    }

    @DeleteMapping("/{tweetId}")
    public ResponseEntity<Void> unlikeTweet(@PathVariable Long tweetId, @RequestHeader String loggedInUser) {
        likeService.unlikeTweet(tweetId, loggedInUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{tweetId}")
    public ResponseEntity<Boolean> isLiked(@PathVariable Long tweetId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(likeService.isLiked(tweetId, loggedInUser));
    }
}
