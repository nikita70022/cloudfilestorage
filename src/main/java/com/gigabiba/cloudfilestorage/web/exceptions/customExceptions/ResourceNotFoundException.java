package com.gigabiba.cloudfilestorage.web.exceptions.customExceptions;

import java.io.IOException;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    ResourceNotFoundException(String message, IOException ex) {super(message, ex);}
}
