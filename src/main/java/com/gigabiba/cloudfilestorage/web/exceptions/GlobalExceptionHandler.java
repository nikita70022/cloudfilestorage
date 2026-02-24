package com.gigabiba.cloudfilestorage.web.exceptions;

import com.gigabiba.cloudfilestorage.web.exceptions.customExceptions.ResourceExistException;
import com.gigabiba.cloudfilestorage.web.exceptions.customExceptions.ResourceNotFoundException;
import com.gigabiba.cloudfilestorage.web.exceptions.customExceptions.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<String> validateExceptionHandler(Exception e,
                                                           HttpServletRequest request) {
        log.error(e.getMessage(), request.getRequestURI(), e);
        return ResponseEntity.status(400).body(e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> userNotAuthenticated(Exception e,
                                                     HttpServletRequest request) {
        log.error("User is not authenticated {}", request.getRequestURI(), e);
        return ResponseEntity.status(401).build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Void> pathNotFound(Exception e,
                                                     HttpServletRequest request) {
        log.error("Resource not found {}", request.getRequestURI(), e);
        return ResponseEntity.status(404).build();
    }

    @ExceptionHandler(ResourceExistException.class)
    public ResponseEntity<String> conflictExceptionHandler(Exception e,
                                                         HttpServletRequest request) {
        log.error("Resource if exist" + request.getRequestURI(), e);
        return ResponseEntity.status(409).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> allExceptionHandler(Exception e,
                                                    HttpServletRequest request) {
        log.error("Unhandled error" + request.getRequestURI(), e);
        return ResponseEntity.internalServerError().build();
    }



}
