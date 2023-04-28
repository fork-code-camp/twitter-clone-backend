package com.example.profile.dto.request;

import java.time.LocalDate;

public record ProfileRequest(
        String username,
        String email,
        LocalDate joinDate
) {

}
