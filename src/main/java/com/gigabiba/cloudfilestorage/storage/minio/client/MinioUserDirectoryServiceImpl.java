package com.gigabiba.cloudfilestorage.storage.minio.client;

import com.gigabiba.cloudfilestorage.exception.storage.ResourceExistsException;
import com.gigabiba.cloudfilestorage.exception.storage.StorageException;
import com.gigabiba.cloudfilestorage.storage.minio.properties.MinioProperties;
import com.gigabiba.cloudfilestorage.storage.service.UserDirectoryService;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.*;

import java.io.ByteArrayInputStream;

@Service
@Setter
@Getter
@Slf4j
public class MinioUserDirectoryServiceImpl implements UserDirectoryService {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioUserDirectoryServiceImpl(MinioClient minioClient, MinioProperties props) {
        this.minioClient = minioClient;
        this.bucketName = props.bucketName();
    }


    public void createUserDirectory(Long id) {

        String userDirectoryName = String.format("user-%s-files", id);

        if (userDirectoryExists(userDirectoryName)) {
            throw new ResourceExistsException(userDirectoryName);
        }

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(userDirectoryName + "/")
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());

        } catch (Exception e) {
            log.error("Unexpected error while create directory. userDirectoryName={}: ", userDirectoryName, e);
            throw new StorageException("Failed to create a directory ", e);
        }
    }


    private boolean userDirectoryExists(String userDirectoryName) {

        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(userDirectoryName + "/")
                            .build()
            );
            return true;

        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())  || "NoSuchObject".equals(e.errorResponse().code())) {
                return false;
            }
            log.error("MinIO error while checking directory of user. userDirectory={}", userDirectoryName, e);
            throw new StorageException(e.getMessage(), e);

        } catch (Exception e) {
            log.error("Unexpected error while checking the directory of user. userDirectory={}", userDirectoryName, e);
            throw new StorageException(e.getMessage(), e);
        }
    }
}
