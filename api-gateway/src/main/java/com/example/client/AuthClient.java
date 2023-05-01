package com.example.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "${feign.auth-client.name}")
public interface AuthClient {

    @GetMapping("api/v1/auth/validate/{jwt}")
    Boolean validateJwt(@PathVariable String jwt);
}
