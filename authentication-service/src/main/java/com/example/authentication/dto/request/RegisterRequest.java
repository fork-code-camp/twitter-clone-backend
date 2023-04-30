package com.example.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email(message = "{email.invalid}") @NotNull(message = "{email.notnull}")
        String email,

        @Size(min = 5, max = 50, message = "{username.size}") @NotNull(message = "{username.notnull}")
        String username,

        @Size(min = 5, max = 25, message = "{password.size}") @NotNull(message = "{password.notnull}")
        String password
) {

}
