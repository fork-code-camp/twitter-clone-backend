package com.example.authentication.client;

import com.example.authentication.client.request.ProfileRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "${services.profile.name}", url = "${services.profile.url}")
public interface ProfileServiceClient {

    @PostMapping(value = "/api/v1/profile")
    String createProfile(@RequestBody ProfileRequest request);
}
