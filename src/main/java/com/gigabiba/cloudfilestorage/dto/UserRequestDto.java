package com.gigabiba.cloudfilestorage.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.gigabiba.cloudfilestorage.storage.util.validation.S3BucketNameValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;



public record UserRequestDto(

        @S3BucketNameValidation
        @NotBlank(message = "Name should not be empty")
        String username,

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Size(
                min = 4,
                max = 12,
                message = "Password must be between 4 and 12 characters long"
        )
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-={}:;\"'<>,.?/]).*$",
                message = "Password must contain at least one uppercase letter, one lowercase letter," +
                        " one number, and one special character"
        )
        @NotBlank(
                message = "Password must be not empty."
        )
        String password) {
}
