package com.gigabiba.cloudfilestorage.config;

import io.minio.*;
import org.modelmapper.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.util.*;


@Configuration
@EnableRedisHttpSession
public class ApplicationConfig {
    @Value("${spring.minio.endpoint}")
    private String URL;
    @Value("${spring.minio.access-key}")
    private String ACCESS_KEY;
    @Value ("${spring.minio.secret-key}")
    private String SECRET_KEY;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(Objects.requireNonNull(URL))
                .credentials(ACCESS_KEY, SECRET_KEY)
                .build();
    }

}
