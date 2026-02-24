package com.gigabiba.cloudfilestorage.web.exceptions.customExceptions;

import java.io.IOException;

public class ResourceExistException extends RuntimeException {
    public ResourceExistException(String message) {
        super(message);
    }
    ResourceExistException(String message, IOException ex) {super(message, ex);}
}
