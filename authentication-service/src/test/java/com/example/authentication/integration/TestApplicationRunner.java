package com.example.authentication.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@ActiveProfiles("test")
public class TestApplicationRunner {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer mockProfileService() {
        return new WireMockServer(new WireMockConfiguration()
                .proxyHostHeader("profile-service")
                .dynamicPort());
    }
}
