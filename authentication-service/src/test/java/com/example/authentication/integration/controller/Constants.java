package com.example.authentication.integration.controller;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public enum Constants {
    AUTH_REQ_PATTERN ("{\"email\": \"%s\", \"password\": \"test\"}"),

    EXISTENT_ACCOUNT_EMAIL ("test@gmail.com"),
    NEW_ACCOUNT_EMAIL ("new_account@gmail.com"),

    EXISTENT_ACCOUNT_JSON (AUTH_REQ_PATTERN.constant.formatted(EXISTENT_ACCOUNT_EMAIL.constant)),
    NEW_ACCOUNT_JSON (AUTH_REQ_PATTERN.constant.formatted(NEW_ACCOUNT_EMAIL.constant)),

    REGISTER_URL ("/api/v1/auth/register"),
    AUTHENTICATE_URL ("/api/v1/auth/authenticate"),
    LOGOUT_URL ("/api/v1/auth/logout"),
    ACTIVATION_URL ("/api/v1/auth/activate"),
    TEST_URL ("/api/v1/test");

    final String constant;
}
