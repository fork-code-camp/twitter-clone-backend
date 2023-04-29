package com.example.authentication.client;

import com.example.authentication.client.request.CreateProfileRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "${services.profile.name}", url = "${services.profile.url}")
public interface ProfileServiceClient {

    @PostMapping(value = "/api/v1/profiles")
    String createProfile(@RequestBody CreateProfileRequest request);
}
