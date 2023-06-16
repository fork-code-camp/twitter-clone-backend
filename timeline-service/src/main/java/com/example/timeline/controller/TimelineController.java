package com.example.timeline.controller;

import com.example.timeline.dto.response.TweetResponse;
import com.example.timeline.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/timelines")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping("/user")
    public ResponseEntity<List<TweetResponse>> getUserTimeline(
            @RequestHeader String loggedInUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(timelineService.getUserTimeline(loggedInUser, PageRequest.of(page, size)));
    }

    @GetMapping("/user-replies")
    public ResponseEntity<List<TweetResponse>> getRepliesUserTimeline(
            @RequestHeader String loggedInUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(timelineService.getRepliesUserTimeline(loggedInUser, PageRequest.of(page, size)));
    }

    @GetMapping("/home")
    public ResponseEntity<List<TweetResponse>> getHomeTimeline(
            @RequestHeader String loggedInUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(timelineService.getHomeTimeline(loggedInUser, PageRequest.of(page, size)));
    }
}
