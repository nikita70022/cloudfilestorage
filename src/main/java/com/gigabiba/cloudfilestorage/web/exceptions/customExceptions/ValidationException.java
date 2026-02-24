package com.gigabiba.cloudfilestorage.web.exceptions.customExceptions;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
