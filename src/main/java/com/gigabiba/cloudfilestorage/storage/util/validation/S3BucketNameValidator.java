package com.gigabiba.cloudfilestorage.storage.util.validation;

import jakarta.validation.*;
import org.springframework.stereotype.*;

import java.util.regex.*;

@Component
public class S3BucketNameValidator implements ConstraintValidator<S3BucketNameValidation, String> {
    
    private static final Pattern BASE_PATTERN =
            Pattern.compile("^[a-z0-9]([a-z0-9-]{1,61})[a-z0-9]$");


    private static final Pattern IPV4_PATTERN =
            Pattern.compile("^(\\d{1,3}\\.){3}\\d{1,3}$");


    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        return isValid(value);
    }


    public static boolean isValid(String value) {

        if (value == null || value.isBlank()) {
            return false;
        }

        String name = value.trim();

        if (name.length() < 3 || name.length() > 63) {
            return false;
        }

        if (!BASE_PATTERN.matcher(name).matches()) {
            return false;
        }

        if (IPV4_PATTERN.matcher(name).matches()) {
            return false;
        }

        if (name.startsWith("xn--") || name.startsWith("sthree-") || name.startsWith("amzn-s3-demo-")) {
            return false;
        }

        if (name.endsWith("-s3alias") || name.endsWith("--ol-s3") || name.endsWith("--x-s3")
                || name.endsWith("--table-s3") || name.endsWith(".mrap")) {
            return false;
        }

        return true;
    }
}
