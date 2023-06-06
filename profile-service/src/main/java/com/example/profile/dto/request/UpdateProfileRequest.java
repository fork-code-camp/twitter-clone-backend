package com.example.profile.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @Size(min = 5, max = 50, message = "{username.size}") @NotNull(message = "{username.not-null}")
        String username,

        @Size(max = 160, message = "{bio.size}") String bio,
        @Size(max = 30, message = "{location.size}") String location,
        @Size(max = 100, message = "{website.size}") String website,

        @Past LocalDate birthDate
) {

}
