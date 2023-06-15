package com.example.timeline.client;

import com.example.timeline.dto.response.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("${services.profile.name}")
public interface ProfileServiceClient {

    @GetMapping("/api/v1/follows/{profileId}/followees")
    List<ProfileResponse> getFollowees(@PathVariable String profileId);

    @GetMapping("/api/v1/follows/{profileId}/followees-celebrities")
    List<ProfileResponse> getFolloweesCelebrities(@PathVariable String profileId);

    @GetMapping("/api/v1/profiles/id/{email}")
    String getProfileIdByLoggedInUser(@PathVariable String email);

    @GetMapping("/api/v1/profiles/{id}")
    ProfileResponse getProfileById(@PathVariable String id);
}
