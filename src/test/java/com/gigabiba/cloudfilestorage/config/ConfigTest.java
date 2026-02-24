package com.gigabiba.cloudfilestorage.config;
import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.*;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.*;
import org.springframework.context.annotation.*;
import org.testcontainers.containers.*;

@TestConfiguration
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
}
