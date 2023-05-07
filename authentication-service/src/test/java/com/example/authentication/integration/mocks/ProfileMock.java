package com.example.authentication.integration.mocks;

import com.example.authentication.client.ProfileServiceClient;
import com.example.authentication.client.request.CreateProfileRequest;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;

public class ProfileMock {

    public static void setupMockProfileResponse(ProfileServiceClient mockService) {
        Mockito
                .doReturn("dummy-profile-id")
                .when(mockService)
                .createProfile(any(CreateProfileRequest.class));
    }
}
