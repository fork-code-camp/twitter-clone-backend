package com.example.tweet.controller;

import com.example.tweet.dto.request.TweetCreateRequest;
import com.example.tweet.dto.request.TweetUpdateRequest;
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
@RequestMapping("/api/v1")
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping("/reply/{replyToId}")
    public ResponseEntity<TweetResponse> reply(
            @RequestPart(required = false) MultipartFile[] files,
            @Valid @RequestPart TweetCreateRequest request,
            @PathVariable Long replyToId,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(replyService.reply(request, replyToId, loggedInUser, files));
    }

    @GetMapping("/replies/user/{profileId}")
    public ResponseEntity<List<TweetResponse>> getAllRepliesForUser(
            @PathVariable String profileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(replyService.getAllRepliesForUser(profileId, PageRequest.of(page, size)));
    }

    @GetMapping("/replies/{replyToId}")
    public ResponseEntity<List<TweetResponse>> getAllRepliesForTweet(@PathVariable Long replyToId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(replyService.getAllRepliesForTweet(replyToId, loggedInUser));
    }

    @GetMapping("/reply/{replyId}")
    public ResponseEntity<TweetResponse> getReply(@PathVariable Long replyId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(replyService.getReply(replyId, loggedInUser));
    }

    @PatchMapping("/reply/{replyId}")
    public ResponseEntity<TweetResponse> updateReply(
            @Valid @RequestPart TweetUpdateRequest request,
            @RequestPart(required = false) MultipartFile[] files,
            @PathVariable Long replyId,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(replyService.updateReply(replyId, request, loggedInUser, files));
    }

    @DeleteMapping("/reply/{replyId}")
    public ResponseEntity<Boolean> deleteReply(@PathVariable Long replyId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(replyService.deleteReply(replyId, loggedInUser));
    }
}
