package com.gigabiba.cloudfilestorage.storage.util.validation;

import com.gigabiba.cloudfilestorage.exception.storage.ValidationException;

import java.util.regex.Pattern;

public class S3Valid {

    private S3Valid() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final Pattern BASE_PATTERN_FOR_NAME =
            Pattern.compile("^(?!/)(?!.*//)(?!.*\\\\)[A-Za-z0-9._\\-/ ]{1,255}$");


    private static final Pattern BASE_PATTERN_FOR_PATH =
            Pattern.compile("^$|^(?:[^/]+/)+$");


    public static void fileNameIsValid(String value) {
        if (value == null || value.isBlank() || !BASE_PATTERN_FOR_NAME.matcher(value.trim()).matches()) {
            throw new ValidationException("incorrect format of name");
        }
    }


    public static void parentIsValid(String value) {
        if (!BASE_PATTERN_FOR_PATH.matcher(value).matches()) {
            throw new ValidationException("incorrect format of path");
        }
    }


    public static void equals(String a, String b) {
        if (a.equals(b)) {
            throw new ValidationException("paths should not be equals");
        }
    }


    public static void isBlank(String name) {
        if (name.isBlank()) {
            throw new ValidationException("path should not be empty");
        }
    }
}
