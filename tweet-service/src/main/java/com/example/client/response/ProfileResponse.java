package com.example.client.response;

import java.time.LocalDate;

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
