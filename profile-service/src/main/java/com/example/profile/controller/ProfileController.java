package com.example.profile.controller;

import com.example.profile.dto.filter.ProfileFilter;
import com.example.profile.dto.request.CreateProfileRequest;
import com.example.profile.dto.request.UpdateProfileRequest;
import com.example.profile.dto.response.PageResponse;
import com.example.profile.dto.response.ProfileResponse;
import com.example.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profiles")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<String> createProfile(@Valid @RequestBody CreateProfileRequest request) {
        return ResponseEntity.status(CREATED).body(profileService.createProfile(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable String id) {
        return ResponseEntity.ok(profileService.getProfile(id));
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getAuthProfile(@RequestHeader String loggedInUser) {
        return ResponseEntity.ok(profileService.getAuthProfile(loggedInUser));
    }

    @GetMapping("/")
    public ResponseEntity<PageResponse<ProfileResponse>> findAllByUsername(
            @RequestBody ProfileFilter filter,
            Pageable pageable
    ) {
        var response = PageResponse.of(profileService.findAllByUsername(filter, pageable));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @PathVariable String id,
            @RequestHeader String loggedInUser
    ) {
        return ResponseEntity.ok(profileService.updateProfile(id, request, loggedInUser));
    }

    @GetMapping("/id/{email}")
    public ResponseEntity<String> getProfileIdByEmail(@PathVariable String email) {
        return ResponseEntity.ok(profileService.getProfileIdByEmail(email));
    }

    @GetMapping("/images/avatar")
    public ResponseEntity<String> getProfileAvatar(@RequestHeader String loggedInUser) {
        return ResponseEntity.ok(profileService.getProfileAvatar(loggedInUser));
    }

    @PostMapping("/images/avatar")
    public ResponseEntity<Boolean> uploadAvatarImage(@RequestParam MultipartFile file, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(profileService.uploadAvatarImage(file, loggedInUser));
    }

    @DeleteMapping("/images/avatar")
    public ResponseEntity<Boolean> deleteAvatarImage(@RequestHeader String loggedInUser) {
        return ResponseEntity.ok(profileService.deleteAvatarImage(loggedInUser));
    }

    @PostMapping("/images/banner")
    public ResponseEntity<Boolean> uploadBannerImage(@RequestParam MultipartFile file, @RequestHeader String loggedInUser) {
        return ResponseEntity.ok(profileService.uploadBannerImage(file, loggedInUser));
    }

    @DeleteMapping("/images/banner")
    public ResponseEntity<Boolean> deleteBannerImage(@RequestHeader String loggedInUser) {
        return ResponseEntity.ok(profileService.deleteBannerImage(loggedInUser));
    }
}
