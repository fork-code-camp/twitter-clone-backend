package com.example.authentication.exception;

public class ActivationCodeExpiredException extends RuntimeException {

    public ActivationCodeExpiredException(String message) {
        super(message);
    }
}
