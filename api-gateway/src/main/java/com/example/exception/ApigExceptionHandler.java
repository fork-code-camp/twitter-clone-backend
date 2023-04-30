package com.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ControllerAdvice
public class ApigExceptionHandler {

    @ExceptionHandler({InvalidTokenException.class, MissingTokenException.class})
    public ResponseEntity<ErrorMessage> handleException(Exception e) {
        return generateErrorMessage(e, UNAUTHORIZED);
    }

    @ExceptionHandler(UnavailableServiceException.class)
    public ResponseEntity<ErrorMessage> handleException(UnavailableServiceException e) {
        return generateErrorMessage(e, SERVICE_UNAVAILABLE);
    }

    private ResponseEntity<ErrorMessage> generateErrorMessage(Exception e, HttpStatus status) {
        ErrorMessage errorMessage = ErrorMessage.builder()
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .status(status.value())
                .build();
        return new ResponseEntity<>(errorMessage, status);
    }
}
