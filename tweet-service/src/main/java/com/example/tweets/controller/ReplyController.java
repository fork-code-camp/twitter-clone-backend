package com.example.tweets.controller;

import com.example.tweets.dto.request.TweetCreateRequest;
import com.example.tweets.dto.response.TweetResponse;
import com.example.tweets.service.ReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/replies")
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping("/{parentTweetId}")
    public ResponseEntity<TweetResponse> reply(
            @Valid @RequestBody TweetCreateRequest tweetCreateRequest,
            @PathVariable Long parentTweetId,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(replyService.reply(tweetCreateRequest, parentTweetId, loggedInUser));
    }

    @GetMapping("/all")
    public ResponseEntity<List<TweetResponse>> findAllRepliesForUser(@RequestHeader String loggedInUser) {
        return ResponseEntity.ok(replyService.findAllRepliesForUser(loggedInUser));
    }

    @GetMapping("/all/{parentTweetId}")
    public ResponseEntity<List<TweetResponse>> findAllRepliesForTweet(@PathVariable Long parentTweetId) {
        return ResponseEntity.ok(replyService.findAllRepliesForTweet(parentTweetId));
    }

}
