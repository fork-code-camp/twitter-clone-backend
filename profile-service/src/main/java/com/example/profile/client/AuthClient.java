package com.example.profile.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "${services.auth.name}")
public interface AuthClient {

    @GetMapping("/api/v1/auth-principal-controller/getPrincipalUsername")
    String getPrincipalUsername(@RequestHeader("Authorization") String bearerToken);
}
