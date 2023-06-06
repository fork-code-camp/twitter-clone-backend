package com.example.tweet.exception;

import com.example.tweet.dto.response.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String field = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(field, errorMessage);
                });
        return new ResponseEntity<>(errors, BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(EntityNotFoundException e) {
        return generateDefaultErrorMessage(e, NOT_FOUND);
    }

    @ExceptionHandler(CreateEntityException.class)
    public ResponseEntity<ErrorResponse> handleException(CreateEntityException e) {
        return generateDefaultErrorMessage(e, UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ActionNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleException(ActionNotAllowedException e) {
        return generateDefaultErrorMessage(e, FORBIDDEN);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return generateDefaultErrorMessage(e, BAD_REQUEST);
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
