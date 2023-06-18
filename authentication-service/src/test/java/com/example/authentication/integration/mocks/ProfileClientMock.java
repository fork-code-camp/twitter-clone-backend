package com.example.authentication.integration.mocks;

import com.example.authentication.client.ProfileServiceClient;
import com.example.authentication.client.request.CreateProfileRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProfileClientMock {

    public static void setupMockProfileResponse(ProfileServiceClient profileServiceClient) {
        when(profileServiceClient.createProfile(any(CreateProfileRequest.class)))
                .thenReturn("dummy-profile-id");
    }
}
