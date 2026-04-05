package com.gigabiba.cloudfilestorage.storage.minio.client;

import com.gigabiba.cloudfilestorage.storage.minio.properties.MinioProperties;
import com.gigabiba.cloudfilestorage.storage.model.NormalizedMultipartFile;
import com.gigabiba.cloudfilestorage.exception.storage.ResourceExistsException;
import com.gigabiba.cloudfilestorage.storage.util.path.PathUtil;
import com.gigabiba.cloudfilestorage.storage.model.FileResponseDto;
import com.gigabiba.cloudfilestorage.storage.model.ObjectResponseDto;
import com.gigabiba.cloudfilestorage.storage.model.Type;
import com.gigabiba.cloudfilestorage.exception.storage.ResourceNotExistsException;
import com.gigabiba.cloudfilestorage.exception.storage.StorageException;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.util.Objects;

@Service
@Slf4j
class MinioFileService {

    private final String bucketName;
    private final MinioClient minioClient;
    private final MinioDirectoryService directoryService;

    protected MinioFileService(MinioClient minioClient, MinioProperties props, MinioDirectoryService directoryService) {
        this.minioClient = minioClient;
        this.bucketName = props.bucketName();
        this.directoryService = directoryService;
    }


    protected static boolean isFile(String name) {

        if (name == null) {
            return false;
        }

        return !(name.endsWith("/") || name.endsWith("__XLDIR__"));
    }


    protected ObjectResponseDto getFileInfo(String userDirectory, String pathName) {

        if (!fileExists(userDirectory, pathName)) {
            throw new ResourceNotExistsException("File not exists: " + pathName);
        }

        try {
            fileExists(userDirectory, pathName);

            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(userDirectory + "/" + pathName)
                            .build()
            );

            return new FileResponseDto(
                    PathUtil.getParentPath(pathName),
                    PathUtil.getName(pathName),
                    stat.size(),
                    Type.FILE
            );

        } catch (Exception e) {

            log.error("Unexpected error while getting info about file. userDirectory={}, pathName={}",
                    userDirectory, pathName, e);
            throw new StorageException("Failed to get file info", e);
        }
    }


    protected ObjectResponseDto putFile(String userDirectory, String path, MultipartFile file) {

        if (fileExists(userDirectory, path + file.getOriginalFilename())) {
            throw new ResourceExistsException("File already exists: " + path);
        }

        directoryService.createDirectoriesIfNotExist(userDirectory,
                path + PathUtil.getParentPath(Objects.requireNonNull(file.getOriginalFilename())));

        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(userDirectory + "/" + path + file.getOriginalFilename())
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

        } catch (Exception e) {

            log.error("Unexpected error while putting file. userDirectory={}, path={}", userDirectory, path, e);
            throw new StorageException("Failed to put file", e);
        }

        return new ObjectResponseDto(
                path,
                file.getOriginalFilename(),
                file.getSize(),
                Type.FILE
        );
    }


    protected StreamingResponseBody getFile(String userDirectory, String pathName) {

        if (!fileExists(userDirectory, pathName)) {
            throw new ResourceNotExistsException("File already exists: " + pathName);
        }

        try {
            return outputStream -> {
                try (InputStream is = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucketName)
                                .object(userDirectory + "/" + pathName)
                                .build()
                )) {

                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

        } catch (Exception e) {

            log.error("Unexpected error while getting file. userDirectory={}, pathName={}", userDirectory, pathName, e);
            throw new StorageException("Failed to get file", e);
        }
    }


    protected void deleteFile(String userDirectory, String pathName) {

        if (pathName == null || pathName.isBlank()) {
            throw new IllegalArgumentException("Path of file must not be empty");
        }
        if (pathName.endsWith("/")) {
            throw new IllegalArgumentException("Cannot delete directory as file");
        }
        if (!fileExists(userDirectory, pathName)) {
            throw new ResourceNotExistsException("File already exists: " + pathName);
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(userDirectory + "/" + pathName)
                            .build());
        } catch (Exception e) {

            log.error("Unexpected error while delete file. userDirectory={}, pathName={}", userDirectory, pathName, e);
            throw new StorageException("Failed to delete file", e);
        }
    }


    protected void moveFile(String userDirectory, String fromPathName, String toPathName) {

        if ((fromPathName == null || fromPathName.isBlank()) || (toPathName == null || toPathName.isBlank())) {
            throw new IllegalArgumentException("Path of file must not be empty");
        }
        if ((fromPathName.endsWith("/")) || (toPathName.endsWith("/"))) {
            throw new IllegalArgumentException("Cannot delete directory as file");
        }
        if (!fileExists(userDirectory, fromPathName)) {
            throw new ResourceNotExistsException("File not exists: " + fromPathName);
        }
        if (fileExists(userDirectory, toPathName)) {
            throw new ResourceExistsException("File already exists: " + toPathName);
        }

        boolean copied = false;
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(userDirectory + "/" + toPathName)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucketName)
                                            .object(userDirectory + "/" + fromPathName)
                                            .build()
                            )
                            .build()
            );
            copied = true;

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(userDirectory + "/" + fromPathName)
                            .build()
            );

        } catch (Exception e) {

            if (copied) {
                try {
                    minioClient.removeObject(
                            RemoveObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(userDirectory + "/" + toPathName)
                                    .build()
                    );

                } catch (Exception rollbackException) {

                    log.error("Rollback failed for " + toPathName + ": " + rollbackException.getMessage());
                }
            }

            throw new IllegalStateException("Move failed, rollback executed", e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean fileExists(String userDirectory, String path) {

        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(userDirectory + "/"  + path)
                            .build()
            );

            return true;

        } catch (ErrorResponseException e) {

            if ("NoSuchKey".equals(e.errorResponse().code())  || "NoSuchObject".equals(e.errorResponse().code())) {
                return false;
            }

            log.error("MinIO error while checking object. userDirectory={}, path={}", userDirectory, path, e);
            throw new StorageException(e.getMessage(), e);

        } catch (Exception e) {

            log.error("Unexpected error while checking object. userDirectory={}, path={}", userDirectory, path, e);
            throw new StorageException(e.getMessage(), e);
        }
    }
}
