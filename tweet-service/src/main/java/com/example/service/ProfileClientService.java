package com.example.service;

import com.example.client.ProfileClient;
import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileClientService {

    private final ProfileClient profileClient;

    public String getProfileId(HttpServletRequest httpServletRequest) {
        String profileId = profileClient.getProfileId(getToken(httpServletRequest));
        log.info("profileId has been successfully received: {}", profileId);

        return profileId;
    }

    public String getProfileUsername(HttpServletRequest httpServletRequest) {
        String profileUsername = profileClient.getProfileUsername(getToken(httpServletRequest));
        log.info("profileUsername has been successfully received: {}", profileUsername);

        return profileUsername;
    }

    private String getToken(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
    }
}
