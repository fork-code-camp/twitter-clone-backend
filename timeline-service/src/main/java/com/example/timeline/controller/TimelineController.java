package com.example.timeline.controller;

import com.example.timeline.dto.response.TweetResponse;
import com.example.timeline.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/timeline")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping("/user")
    public ResponseEntity<List<TweetResponse>> getUserTimelineForLoggedInUser(
            @RequestHeader String loggedInUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(timelineService.getUserTimelineForLoggedInUser(loggedInUser, PageRequest.of(page, size)));
    }

    @GetMapping("/user/{profileId}")
    public ResponseEntity<List<TweetResponse>> getUserTimelineForAnotherUser(
            @PathVariable String profileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(timelineService.getUserTimelineForAnotherInUser(profileId, PageRequest.of(page, size)));
    }

    @GetMapping("/user-replies")
    public ResponseEntity<List<TweetResponse>> getRepliesUserTimelineForLoggedInUser(
            @RequestHeader String loggedInUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(timelineService.getRepliesUserTimelineForLoggedInUser(loggedInUser, PageRequest.of(page, size)));
    }

    @GetMapping("/user-replies/{profileId}")
    public ResponseEntity<List<TweetResponse>> getRepliesUserTimelineForAnotherUser(
            @PathVariable String profileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(timelineService.getRepliesUserTimelineForAnotherInUser(profileId, PageRequest.of(page, size)));
    }

    @GetMapping("/home")
    public ResponseEntity<List<TweetResponse>> getHomeTimeline(
            @RequestHeader String loggedInUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(timelineService.getHomeTimelineForLoggedInUser(loggedInUser, PageRequest.of(page, size)));
    }
}
