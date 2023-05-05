package com.example.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("${services.profile.name}")
public interface ProfileClient {

    @GetMapping("/api/v1/secured-profiles/get-profileId")
    String getProfileId(@RequestHeader("Authorization") String bearerToken);

    @GetMapping("/api/v1/secured-profiles/get-profileUsername")
    String getProfileUsername(@RequestHeader("Authorization") String bearerToken);
}
