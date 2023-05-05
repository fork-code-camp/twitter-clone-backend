package com.example.exception;

public class CreateEntityException extends RuntimeException{
    public CreateEntityException(String message) {
        super(message);
    }
}
