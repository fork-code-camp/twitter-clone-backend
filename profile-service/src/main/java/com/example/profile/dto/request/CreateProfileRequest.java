package com.example.profile.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateProfileRequest(
        @Email(message = "{email.invalid}") @NotNull(message = "{email.not-null}")
        String email,

        @Size(min = 5, max = 50, message = "{username.size}") @NotNull(message = "{username.notblank}")
        String username,

        @NotNull(message = "{joinDate.not-null}") LocalDate joinDate
) {

}
