package com.gigabiba.cloudfilestorage.services.minioService;

import com.gigabiba.cloudfilestorage.web.exceptions.customExceptions.ResourceExistException;
import com.gigabiba.cloudfilestorage.web.exceptions.customExceptions.ResourceNotFoundException;
import com.gigabiba.cloudfilestorage.utils.MyPath;
import com.gigabiba.cloudfilestorage.web.dto.minio.MinioFile;
import com.gigabiba.cloudfilestorage.web.dto.minio.MinioObject;
import com.gigabiba.cloudfilestorage.web.dto.minio.Type;
import io.minio.*;
import io.minio.errors.*;
import lombok.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.ByteBuffer;

@Service
class MinioFileService {

    private final MinioClient minioClient;
    private final MinioDirectoryService directoryService;

    @Autowired
    protected MinioFileService(MinioClient minioClient, MinioDirectoryService directoryService) {
        this.minioClient = minioClient;
        this.directoryService = directoryService;
    }

    @SneakyThrows
    protected void putFile(String username, String path, MultipartFile file) throws IllegalStateException {
        fileExist(username, path + MyPath.normalized(file.getOriginalFilename()));
        directoryService.createDirectoriesIfNotExist(username, path + MyPath.getParent(file.getOriginalFilename()));

        try (InputStream is = file.getInputStream()) {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(username)
                            .object(path + MyPath.normalized(file.getOriginalFilename()))
                            .stream(is, is.available(), -1)
                            .contentType(file.getContentType())
                            .build());
        }
    }

    @SneakyThrows
    protected StreamingResponseBody getFile(String username, String path) {
        StreamingResponseBody stream = null;
        directoryService.parentNotExist(username, path);

        stream = outputStream -> {
            try (InputStream is = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(username)
                            .object(path)
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
        return stream;
    }

    @SneakyThrows
    protected void copyFile(String username, String from, String to) throws IllegalStateException {
        boolean bucketFound =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(username).build());
        if (bucketFound) {
            fileFound(username, from);
            fileExist(username, to);
            directoryService.parentNotExist(username, MyPath.getParent(to) + "/");

            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(username)
                            .object(to)
                            .source(
                                    CopySource.builder()
                                            .bucket(username)
                                            .object(from)
                                            .build()
                            )
                            .build()
            );
        }
    }

    @SneakyThrows
    protected void deleteFile(String username, String pathWithFileName) throws IllegalStateException {
        boolean bucketFound =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(username).build());
        if (bucketFound) {
            fileFound(username, pathWithFileName);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(username)
                            .object(pathWithFileName)
                            .build());
        }
    }

    @SneakyThrows
    protected MinioObject getFileInfo(String username, String path) {
        MinioFile info = new MinioFile();
        fileFound(username, path);

        StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(username)
                        .object(path)
                        .build()
        );

        info.setPath(MyPath.getParent(path));
        info.setName(MyPath.getName(path));
        info.setSize(String.valueOf(((double) stat.size()/1024)));
        info.setType(Type.FILE);

        return info;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @SneakyThrows
    private void fileExist(String username, String path) throws IllegalStateException {
        boolean found = false;
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(username)
                            .object(path)
                            .build()
            );
            found = true;
        } catch (ErrorResponseException e) {
            if (!"NoSuchKey".equals(e.errorResponse().code())) {
                throw e;
            }
        }

        if (found) {
            throw new ResourceExistException("the file already exists " + MyPath.getName(path));
        }
    }

    @SneakyThrows
    private void fileFound(String username, String path) throws IllegalStateException {
        boolean found = false;
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(username)
                            .object(path)
                            .build()
            );
            found = true;
        } catch (ErrorResponseException e) {
            if (!"NoSuchKey".equals(e.errorResponse().code())) {
                throw e;
            }
        }

        if (found == false) {
            throw new ResourceNotFoundException("the file not found " + MyPath.getName(path));
        }
    }
}
