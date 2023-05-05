package com.example.profile.exception;

public class NonAuthorizedException extends RuntimeException {

    public NonAuthorizedException(String message) {
        super(message);
    }
}
