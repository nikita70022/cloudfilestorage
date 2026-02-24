package com.gigabiba.cloudfilestorage.utils.minioValidation;

import com.gigabiba.cloudfilestorage.web.exceptions.customExceptions.ValidationException;

import java.util.regex.*;

public class S3Valid {

    private static final Pattern BASE_PATTERN_FOR_NAME =
            Pattern.compile("^(?!/)(?!.*//)(?!.*\\\\)[A-Za-z0-9._\\-/]{1,255}$");

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
        if (a.equalsIgnoreCase(b)) {
            throw new ValidationException("incorrect format of path");
        }
    }

    public static void isBlank(String name) {
        if (name.isBlank()) {
            throw new ValidationException("incorrect format of path");
        }
    }

    public static boolean isDirectory(String path) {
        return path.endsWith("/");
    }

    public static boolean isFile(String name) {
        return !name.endsWith("/") && !name.endsWith("__XLDIR__");
    }
}
