package com.example.tweet.integration.mocks;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.client.response.ProfileResponse;

import java.time.LocalDate;
import java.util.Collections;

import static com.example.tweet.integration.constants.GlobalConstants.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ProfileClientMock {

    public static void setupProfileClientResponse(ProfileServiceClient profileServiceClient) {
        ProfileResponse response = ProfileResponse.builder()
                .username(USERNAME.getConstant())
                .email(EMAIL.getConstant())
                .joinDate(LocalDate.MIN)
                .bio("some bio")
                .location("some location")
                .birthDate(LocalDate.MIN)
                .website("some website")
                .avatarUrl("some avatar url")
                .bannerUrl("some banner url")
                .build();

        when(profileServiceClient.getProfileIdByLoggedInUser(EMAIL.getConstant()))
                .thenReturn(ID.getConstant());

        when(profileServiceClient.getProfileById(ID.getConstant()))
                .thenReturn(response);

        when(profileServiceClient.getFollowers(anyString()))
                .thenReturn(Collections.emptyList());
    }
}
