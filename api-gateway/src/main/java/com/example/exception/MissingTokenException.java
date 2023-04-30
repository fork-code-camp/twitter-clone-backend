package com.example.exception;

public class MissingTokenException extends RuntimeException{

    public MissingTokenException(String message) {
        super(message);
    }
}
