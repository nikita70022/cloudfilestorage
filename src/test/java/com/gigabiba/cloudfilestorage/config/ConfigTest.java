package com.gigabiba.cloudfilestorage.config;

import com.redis.testcontainers.RedisContainer;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestConfiguration
@Testcontainers
@Slf4j
public class ConfigTest {


    @Bean
    @ServiceConnection
    public static PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:17.7-alpine")
                .withDatabaseName("test")
                .withUsername("test")
                .withPassword("test");
    }

    @Bean
    @ServiceConnection
    public static RedisContainer redisContainer() {
        return new RedisContainer("redis:7-alpine");
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public static MinIOContainer minio() {
        return new MinIOContainer("minio/minio:RELEASE.2025-09-07T16-13-09Z")
                .withUserName("minio")
                .withPassword("minio123");
    }

    @Bean
    public MinioClient minioClient(MinIOContainer minio) {
        System.out.println("MinIO running: " + minio.isRunning() + " " + minio.getS3URL());

        return MinioClient.builder()
                .endpoint(minio.getS3URL())
                .credentials("minio", "minio123")
                .build();
    }


    @DynamicPropertySource
    static void containersProperties(DynamicPropertyRegistry registry, MinIOContainer minio) {
        System.out.println("MinIO running: " + minio.isRunning() + " " + minio.getS3URL());

        registry.add("app.minio.endpoint", () -> minio.getS3URL());
        registry.add("app.minio.credentials.access-key", () -> "minio");
        registry.add("app.minio.credentials.secret-key", () -> "minio123");
    }
}