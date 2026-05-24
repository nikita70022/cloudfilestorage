package com.gigabiba.cloudfilestorage.storage.util.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = S3BucketNameValidator.class)
public @interface S3BucketNameValidation {

    String message() default "Invalid name of S3 bucket";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
