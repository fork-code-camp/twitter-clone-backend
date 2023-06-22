package com.example.tweet.controller;

import com.example.tweet.dto.request.TweetCreateRequest;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.service.ReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/replies")
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping("/{parentTweetId}")
    public ResponseEntity<TweetResponse> reply(
            @RequestPart(required = false) MultipartFile[] files,
            @Valid @RequestPart TweetCreateRequest request,
            @PathVariable Long parentTweetId,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(replyService.reply(request, parentTweetId, loggedInUser, files));
    }

    @GetMapping("/user/{profileId}")
    public ResponseEntity<List<TweetResponse>> getAllRepliesForUser(
            @PathVariable String profileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(replyService.getAllRepliesForUser(profileId, PageRequest.of(page, size)));
    }

    @GetMapping("/{parentTweetId}")
    public ResponseEntity<List<TweetResponse>> getAllRepliesForTweet(@PathVariable Long parentTweetId) {
        return ResponseEntity.ok(replyService.getAllRepliesForTweet(parentTweetId));
    }

}
