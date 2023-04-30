package com.example.profile.controller;

import com.example.profile.entity.Profile;
import com.example.profile.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/follows")
public class FollowController {

    private final FollowService followService;

    @GetMapping("/{followeeId}")
    private ResponseEntity<Boolean> isFollowed(@PathVariable String followeeId, Authentication authentication) {
        return ResponseEntity.ok(followService.isFollowed(followeeId, authentication.name()));
    }

    @PostMapping("/{followeeId}")
    public ResponseEntity<Boolean> follow(@PathVariable String followeeId, Authentication authentication) {
        return ResponseEntity.ok(followService.follow(followeeId, authentication.name()));
    }

    @DeleteMapping("/{followeeId}")
    public ResponseEntity<Boolean> unfollow(@PathVariable String followeeId, Authentication authentication) {
        return ResponseEntity.ok(followService.unfollow(followeeId, authentication.name()));
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
