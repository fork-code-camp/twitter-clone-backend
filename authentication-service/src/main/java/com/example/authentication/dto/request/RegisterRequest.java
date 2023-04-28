package com.example.authentication.dto.request;

public record RegisterRequest(
        String username,
        String email,
        String password
) {

}
