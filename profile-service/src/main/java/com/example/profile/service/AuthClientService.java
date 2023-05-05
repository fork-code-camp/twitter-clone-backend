package com.example.profile.service;

import com.example.profile.client.AuthClient;
import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthClientService {

    private final AuthClient authClient;

    public String getUserEmail(HttpServletRequest httpServletRequest) {
        String bearerToken = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);

        String email = authClient.getPrincipalUsername(bearerToken);
        log.info("Email {} has been successfully extracted", email);

        return email;
    }
}
