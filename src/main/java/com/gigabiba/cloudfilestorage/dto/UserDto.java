package com.gigabiba.cloudfilestorage.dto;

import com.fasterxml.jackson.annotation.*;
import com.gigabiba.cloudfilestorage.security.service.Role;
import com.gigabiba.cloudfilestorage.storage.util.validation.S3BucketNameValidation;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Long id;

    @S3BucketNameValidation
    @NotBlank(message = "Name should not be empty")
    private String username;


    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(
            min = 5,
            max = 12,
            message = "Password must be between 5 and 12 characters long"
    )
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-={}:;\"'<>,.?/]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter," +
                    " one number, and one special character"
    )
    @NotBlank(
            message = "Password must be not empty."
    )
    private String password;

    private Role role;

}
