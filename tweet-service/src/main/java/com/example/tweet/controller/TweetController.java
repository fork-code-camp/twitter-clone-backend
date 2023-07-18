package com.example.tweet.controller;

import com.example.tweet.dto.request.TweetCreateRequest;
import com.example.tweet.dto.request.TweetUpdateRequest;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.service.TweetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TweetController {

    private final TweetService tweetService;

    @PostMapping("/tweet")
    public ResponseEntity<TweetResponse> createTweet(
            @Valid @RequestPart TweetCreateRequest request,
            @RequestPart(required = false) MultipartFile[] files,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(tweetService.createTweet(request, loggedInUser, files));
    }

    @PostMapping(value = "/tweet/{tweetId}")
    public ResponseEntity<TweetResponse> createQuoteTweet(
            @Valid @RequestPart TweetCreateRequest request,
            @RequestPart(required = false) MultipartFile[] files,
            @PathVariable Long tweetId,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(tweetService.createQuoteTweet(request, tweetId, loggedInUser, files));
    }

    @GetMapping("/tweet/{tweetId}")
    public ResponseEntity<TweetResponse> getTweet(@PathVariable Long tweetId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(tweetService.getTweet(tweetId, loggedInUser));
    }

    @GetMapping("/tweets/user/{profileId}")
    public ResponseEntity<List<TweetResponse>> getAllTweetsForUser(
            @PathVariable String profileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(tweetService.getAllTweetsForUser(profileId, PageRequest.of(page, size)));
    }

    @PatchMapping(value = "/tweet/{tweetId}")
    public ResponseEntity<TweetResponse> updateTweet(
            @Valid @RequestPart TweetUpdateRequest request,
            @RequestPart(required = false) MultipartFile[] files,
            @PathVariable Long tweetId,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(tweetService.updateTweet(tweetId, request, loggedInUser, files));
    }

    @DeleteMapping("/tweet/{tweetId}")
    public ResponseEntity<Boolean> deleteTweet(
            @PathVariable Long tweetId,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(tweetService.deleteTweet(tweetId, loggedInUser));
    }
}
