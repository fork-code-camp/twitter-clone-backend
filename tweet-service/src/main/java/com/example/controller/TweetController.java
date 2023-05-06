package com.example.controller;

import com.example.dto.request.TweetCreateRequest;
import com.example.dto.request.TweetUpdateRequest;
import com.example.dto.response.TweetResponse;
import com.example.service.LikeService;
import com.example.service.TweetService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tweets")
@RequiredArgsConstructor
public class TweetController {

    private final TweetService tweetService;
    private final LikeService likeService;

    @GetMapping("/{id}")
    public ResponseEntity<TweetResponse> getTweet(
            @PathVariable Long id,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(tweetService.getTweet(id, httpServletRequest));
    }



    @PostMapping("/post")
    public ResponseEntity<TweetResponse> postTweet(
            @RequestBody TweetCreateRequest tweetCreateRequest,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(tweetService.postTweet(tweetCreateRequest, httpServletRequest));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TweetResponse> updateTweet(
            @PathVariable Long id,
            @RequestBody TweetUpdateRequest tweetUpdateRequest,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(tweetService.updateTweet(id, tweetUpdateRequest, httpServletRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTweet(
            @PathVariable Long id
    ) {
        tweetService.deleteTweet(id);
        return ResponseEntity.ok("Your tweet has been successfully deleted!");
    }
}
