package com.example.authentication.exception;

public class ActivationCodeNotFoundException extends RuntimeException {

    public ActivationCodeNotFoundException(String message) {
        super(message);
    }
}
