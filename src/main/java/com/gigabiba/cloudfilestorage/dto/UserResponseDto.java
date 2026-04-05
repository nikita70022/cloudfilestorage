package com.gigabiba.cloudfilestorage.dto;

import jakarta.validation.constraints.NotBlank;

public record UserResponseDto(@NotBlank(message = "Name should not be empty") String username) {}
