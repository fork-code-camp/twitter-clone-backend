package com.example.profile.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProfileRequest(
        @Email(message = "{email.invalid}") @NotNull(message = "{email.notnull}")
        String email,

        @Size(min = 5, max = 50, message = "{username.size}") @NotNull(message = "{username.notnull}")
        String username,

        @Past @NotNull(message = "{joinDate.notnull}") LocalDate joinDate
) {

}
