package com.example.authentication.integration.mocks;

import com.example.authentication.client.ProfileServiceClient;
import com.example.authentication.client.request.CreateProfileRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.mockito.ArgumentMatchers.any;

public class ProfileMock {

    public static void setupMockProfileResponse(ProfileServiceClient mockService) {
        Mockito
                .doReturn("dummy-profile-id")
                .when(mockService)
                .createProfile(any(CreateProfileRequest.class));
    }
}
