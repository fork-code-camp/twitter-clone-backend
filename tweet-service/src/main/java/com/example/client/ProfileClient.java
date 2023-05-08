package com.example.client;

import com.example.client.response.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("${services.profile.name}")
public interface ProfileClient {

    @GetMapping("/api/v1/profiles/id/{email}")
    String getProfileIdByLoggedInUser(@PathVariable String email);

    @GetMapping("/api/v1/profiles/{id}")
    ProfileResponse getProfileById(@PathVariable String id);
}
