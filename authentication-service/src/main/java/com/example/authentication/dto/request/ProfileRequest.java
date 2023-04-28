package com.example.authentication.dto.request;

import java.time.LocalDate;

public record ProfileRequest(
        String username,
        String email,
        LocalDate joinDate
) {

}
