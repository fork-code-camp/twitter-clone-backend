package com.example.apigateway.exception;

public class MissingTokenException extends RuntimeException {

    public MissingTokenException(String message) {
        super(message);
    }
}
