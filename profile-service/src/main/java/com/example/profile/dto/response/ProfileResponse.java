package com.example.profile.dto.response;

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
