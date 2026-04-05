package com.gigabiba.cloudfilestorage.config;

import com.gigabiba.cloudfilestorage.config.properties.FrontendProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


@Configuration
@EnableRedisHttpSession
@EnableConfigurationProperties(FrontendProperties.class)
public class ApplicationConfig {
}
