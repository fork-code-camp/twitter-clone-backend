package com.example.authentication.dto.request;

public record AuthenticationRequest(
        String email,
        String password
) {

}
