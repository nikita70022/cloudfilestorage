package com.gigabiba.cloudfilestorage.storage.minio.properties;


import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.minio")
@Validated
public record MinioProperties(
        @NotBlank String endpoint,
        @NotBlank String bucketName
) {}
