package com.gigabiba.cloudfilestorage.web.dto;

import com.fasterxml.jackson.annotation.*;
import com.gigabiba.cloudfilestorage.utils.minioValidation.S3BucketNameValidation;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;

public class UserDto {

    public UserDto() {}

    public UserDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public UserDto(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    private Long id;

    @S3BucketNameValidation
    @NotBlank(message = "Name should not be empty")
    private String username;


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
    @NotNull(
            message = "Password must be not null."
    )
    @NotBlank(
            message = "Password must be not empty."
    )
    private String password;

    private String role;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "Name should not be empty") String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank(message = "Name should not be empty") String username) {
        this.username = username;
    }

    public @Size(
            min = 4,
            max = 12,
            message = "Password must be between 4 and 12 characters long"
    ) @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-={}:;\"'<>,.?/]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter," +
                    " one number, and one special character"
    ) @NotNull(
            message = "Password must be not null."
    ) @NotBlank(
            message = "Password must be not empty."
    ) String getPassword() {
        return password;
    }

    public void setPassword(@Size(
            min = 4,
            max = 12,
            message = "Password must be between 4 and 12 characters long"
    ) @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-={}:;\"'<>,.?/]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter," +
                    " one number, and one special character"
    ) @NotNull(
            message = "Password must be not null."
    ) @NotBlank(
            message = "Password must be not empty."
    ) String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
