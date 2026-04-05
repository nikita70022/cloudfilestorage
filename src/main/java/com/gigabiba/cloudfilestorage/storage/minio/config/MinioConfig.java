package com.gigabiba.cloudfilestorage.storage.minio.config;

import com.gigabiba.cloudfilestorage.storage.minio.properties.MinioCredentials;
import com.gigabiba.cloudfilestorage.storage.minio.properties.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableConfigurationProperties({MinioCredentials.class, MinioProperties.class})
public class MinioConfig {


    @Bean
    @Primary
    @Profile("!test")
    public MinioClient minioClient(MinioProperties props, MinioCredentials creds) {
        return MinioClient.builder()
                .endpoint(props.endpoint())
                .credentials(creds.accessKey(), creds.secretKey())
                .build();
    }


    @Bean
    @Profile("!test")
    public CommandLineRunner initializationBucket(MinioClient minio, MinioProperties props) {
        return args -> {
            boolean exists = minio.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(props.bucketName())
                            .build());
            if (!exists) {
                minio.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(props.bucketName())
                                .build());

            }
        };
    }
}

