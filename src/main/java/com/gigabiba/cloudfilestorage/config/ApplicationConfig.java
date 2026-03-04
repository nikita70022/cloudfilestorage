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

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public MinioClient minioClient(MinioProperties props) {
        return MinioClient.builder()
                .endpoint(props.getEndpoint())
                .credentials(props.getAccessKey(), props.getSecretKey())
                .build();
    }

}
