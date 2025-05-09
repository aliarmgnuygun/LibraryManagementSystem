package com.getir.aau.librarymanagementsystem.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ExceptionResult> handleResourceNotFoundException(ResourceNotFoundException exception) {
        return new ResponseEntity<>(
                new ExceptionResult(HttpStatus.NOT_FOUND.value(), exception.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ExceptionResult> handleNoHandlerFoundException(NoHandlerFoundException exception) {
        log.error("No handler found for request: {}", exception.getRequestURL(), exception);
        return new ResponseEntity<>(
                new ExceptionResult(HttpStatus.NOT_FOUND.value(), "The requested resource does not exist"),
                HttpStatus.NOT_FOUND
        );
    }
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ExceptionResult> handleResourceAlreadyExistsException(ResourceAlreadyExistsException exception) {
        return new ResponseEntity<>(
                new ExceptionResult(HttpStatus.CONFLICT.value(), exception.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionResult> handleValidationExceptions(MethodArgumentNotValidException exception) {
        Map<String, String> validationErrors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        String errorMessage = "Validation failed: " + validationErrors;

        return new ResponseEntity<>(
                new ExceptionResult(HttpStatus.BAD_REQUEST.value(), errorMessage),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ExceptionResult> handleAllUncaughtException(Exception exception) {
        log.error("Unexpected error occurred", exception);
        return new ResponseEntity<>(
                new ExceptionResult(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected error occurred. Please try again later."
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}