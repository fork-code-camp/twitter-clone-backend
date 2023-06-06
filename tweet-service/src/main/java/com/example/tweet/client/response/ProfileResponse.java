package com.example.tweet.client.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ProfileResponse(
        String username,
        String email,
        LocalDate joinDate,

        String bio,
        String location,
        String website,
        LocalDate birthDate,

        String avatarUrl,
        String bannerUrl
) {

}
