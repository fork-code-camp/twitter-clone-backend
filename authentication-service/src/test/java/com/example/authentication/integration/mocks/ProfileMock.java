package com.example.authentication.integration.mocks;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class ProfileMock {

    public static void setupMockProfileResponse(WireMockServer mockService) {
        mockService.stubFor(WireMock.post(WireMock.urlEqualTo("/api/v1/profiles"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("dummy-profile-id")
                )
        );
    }
}
