package com.example.tweets.exception;

public class CreateEntityException extends RuntimeException {
    public CreateEntityException(String message) {
        super(message);
    }
}
