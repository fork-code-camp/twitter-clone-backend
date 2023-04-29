package com.example.authentication.client.request;

import java.time.LocalDate;

public record CreateProfileRequest(
        String username,
        String email,
        LocalDate joinDate
) {

}
