package com.example.exception;

import com.example.dto.response.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(EntityNotFoundException e) {
        return generateDefaultErrorMessage(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CreateEntityException.class)
    public ResponseEntity<ErrorResponse> handleException(CreateEntityException e) {
        return generateDefaultErrorMessage(e, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private ResponseEntity<ErrorResponse> generateDefaultErrorMessage(Exception e, HttpStatus httpStatus) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(e.getMessage())
                .code(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, httpStatus);
    }
}
