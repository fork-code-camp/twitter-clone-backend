package com.example.authentication.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ErrorResponse {

    private int code;
    private String message;
    private long timestamp;
}

