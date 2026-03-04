package com.gigabiba.cloudfilestorage.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.minio")
public class MinioProperties {
    @NotBlank private String endpoint;
    @NotBlank private String accessKey;
    @NotBlank private String secretKey;
}
