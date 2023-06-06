package com.example.profile.controller;

import com.example.profile.dto.response.ProfileResponse;
import com.example.profile.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/follows")
public class FollowController {

    private final FollowService followService;

    @GetMapping("/{followeeId}")
    public ResponseEntity<Boolean> isFollowed(@PathVariable String followeeId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(followService.isFollowed(followeeId, loggedInUser));
    }

    @PostMapping("/{followeeId}")
    public ResponseEntity<Boolean> follow(@PathVariable String followeeId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(followService.follow(followeeId, loggedInUser));
    }

    @DeleteMapping("/{followeeId}")
    public ResponseEntity<Boolean> unfollow(@PathVariable String followeeId, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(followService.unfollow(followeeId, loggedInUser));
    }

    @GetMapping("/{profileId}/followers")
    public ResponseEntity<List<ProfileResponse>> getFollowers(@PathVariable String profileId) {
        return ResponseEntity.ok(followService.getFollowers(profileId));
    }

    @GetMapping("/{profileId}/followees")
    public ResponseEntity<List<ProfileResponse>> getFollowees(@PathVariable String profileId) {
        return ResponseEntity.ok(followService.getFollowees(profileId));
    }
}
