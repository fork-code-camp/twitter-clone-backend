package com.example.profile.controller;

import com.example.profile.entity.Profile;
import com.example.profile.service.AuthClientService;
import com.example.profile.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/secured-profiles")
@RequiredArgsConstructor
public class SecuredProfileController {

    private final ProfileService profileService;

    @GetMapping("/get-profileId")
    public ResponseEntity<String> getProfileId(HttpServletRequest httpServletRequest) {
        Profile profile = profileService.getProfileEntityByEmail(httpServletRequest);

        return ResponseEntity.ok(profile.getId());
    }

    @GetMapping("/get-profileUsername")
    public ResponseEntity<String> getProfileUsername(HttpServletRequest httpServletRequest) {
        Profile profile = profileService.getProfileEntityByEmail(httpServletRequest);

        return ResponseEntity.ok(profile.getUsername());
    }
}
