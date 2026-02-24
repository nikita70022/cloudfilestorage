package com.gigabiba.cloudfilestorage.services.minioService;

import com.gigabiba.cloudfilestorage.models.*;
import io.minio.*;
import lombok.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Service
public class MinioBucketService {

    private final MinioClient minioClient;

    @Autowired
    public MinioBucketService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @SneakyThrows
    public void bucketExist(String username) {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(username).build())) {
            throw new Exception();
        }
    }

    @SneakyThrows
    public void createBucket(String username) {
        boolean found =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(username).build());
        if (!found) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(username)
                            .build()
            );
        }
    }

    @SneakyThrows
    public void deleteBucket(User user) {
        boolean found =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(user.getUsername()).build());
        if (found) {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(user.getUsername()).build());
        }
    }
}
