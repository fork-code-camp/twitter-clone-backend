package com.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class ApigExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorMessage> handleException(InvalidTokenException e) {
        return generateErrorMessage(e, UNAUTHORIZED);
    }

    @ExceptionHandler(MissingTokenException.class)
    public ResponseEntity<ErrorMessage> handleException(MissingTokenException e) {
        return generateErrorMessage(e, UNAUTHORIZED);
    }

    @ExceptionHandler(UnavailableServiceException.class)
    public ResponseEntity<ErrorMessage> handleException(UnavailableServiceException e) {
        return generateErrorMessage(e, SERVICE_UNAVAILABLE);
    }

    private ResponseEntity<ErrorMessage> generateErrorMessage(Exception e, HttpStatus status) {
        ErrorMessage errorMessage = ErrorMessage.builder()
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .build();
        return new ResponseEntity<>(errorMessage, status);
    }
}
