package com.example.profile.controller;

import com.example.profile.entity.Profile;
import com.example.profile.service.FollowService;
import jakarta.servlet.http.HttpServletRequest;
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
    private ResponseEntity<Boolean> isFollowed(@PathVariable String followeeId, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(followService.isFollowed(followeeId, httpServletRequest));
    }

    @PostMapping("/{followeeId}")
    public ResponseEntity<Boolean> follow(@PathVariable String followeeId, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(followService.follow(followeeId, httpServletRequest));
    }

    @DeleteMapping("/{followeeId}")
    public ResponseEntity<Boolean> unfollow(@PathVariable String followeeId, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(followService.unfollow(followeeId, httpServletRequest));
    }

    @GetMapping("/followers/{profileId}")
    public ResponseEntity<List<Profile>> getFollowers(@PathVariable String profileId) {
        return ResponseEntity.ok(followService.getFollowers(profileId));
    }

    @GetMapping("/followees/{profileId}")
    public ResponseEntity<List<Profile>> getFollowees(@PathVariable String profileId) {
        return ResponseEntity.ok(followService.getFollowees(profileId));
    }
}
