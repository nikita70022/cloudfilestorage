package com.gigabiba.cloudfilestorage.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.nginx")
@Validated
public record FrontendProperties(
        @NotBlank String url
) {}
