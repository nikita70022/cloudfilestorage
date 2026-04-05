package com.gigabiba.cloudfilestorage.storage.util.validation;

import jakarta.validation.*;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = S3BucketNameValidator.class)
public @interface S3BucketNameValidation {

    String message() default "Invalid name of S3 bucket";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
