package com.pocket.spring.application.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<Map<String, String>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(ApiException.UserNameAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserNameAlreadyExistsException(ApiException.UserNameAlreadyExistsException ex) {
        return buildErrorResponse("Username is already taken: " + ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ApiException.UserIdNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserIdNotFoundException(ApiException.UserIdNotFoundException ex) {
        return buildErrorResponse("User id not found: " + ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ApiException.InvalidRequestField.class)
    public ResponseEntity<Map<String, String>> handleInvalidRequestField(ApiException.InvalidRequestField ex) {
        return buildErrorResponse("Invalid field: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApiException.InvalidEmailFormat.class)
    public ResponseEntity<Map<String, String>> handleInvalidEmailFormat(ApiException.InvalidEmailFormat ex) {
        return buildErrorResponse("Invalid email format: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApiException.UserIsInactive.class)
    public ResponseEntity<Map<String, String>> UserIsInactive(ApiException.UserIsInactive ex) {
        return buildErrorResponse("User is inactive: " + ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        logger.error(ex.getMessage());
        return new ResponseEntity<>("General System Error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
