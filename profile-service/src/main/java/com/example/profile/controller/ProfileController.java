package com.example.profile.controller;

import com.example.profile.dto.request.ProfileRequest;
import com.example.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<String> createProfile(@RequestBody ProfileRequest profileRequest) {
        return ResponseEntity.ok(profileService.createProfile(profileRequest)); // return generated profile id
    }
}
