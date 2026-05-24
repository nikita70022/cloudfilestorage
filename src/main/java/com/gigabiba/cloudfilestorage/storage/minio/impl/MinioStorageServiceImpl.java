package com.gigabiba.cloudfilestorage.storage.minio.impl;

import com.gigabiba.cloudfilestorage.exception.storage.StorageException;
import com.gigabiba.cloudfilestorage.storage.minio.properties.MinioProperties;
import com.gigabiba.cloudfilestorage.storage.dto.*;
import com.gigabiba.cloudfilestorage.storage.service.StorageService;
import com.gigabiba.cloudfilestorage.storage.util.path.PathUtil;
import com.gigabiba.cloudfilestorage.storage.util.validation.S3Valid;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements StorageService {

    private final MinioFileService fileService;
    private final MinioDirectoryService directoryService;


    public ObjectResponseDto getFileInfo(Long id, String path) {

        S3Valid.fileNameIsValid(path);

        String userDirectory = String.format("user-%s-files", id);

        return fileService.getFileInfo(userDirectory, path);
    }


    public StreamingResponseBody downloadObject(Long id, String path) {

        String userDirectory = String.format("user-%s-files", id);

        if (MinioDirectoryService.isDirectory(path)) {
            S3Valid.parentIsValid(path);

            return directoryService.getDirectory(userDirectory, path);
        }

        S3Valid.fileNameIsValid(path);

        return fileService.getFile(userDirectory, path);
    }


    public List<ObjectResponseDto> uploadObjects(Long id, String path, List<MultipartFile> files) {

        String userDirectory = String.format("user-%s-files", id);

        log.info("Uploading {} files to storage. userDirectory={}, path={}", files.size(), userDirectory, path);

        S3Valid.parentIsValid(path);

        List<ObjectResponseDto> result = new ArrayList<>();

        for (MultipartFile file : files) {
            MultipartFile normalizedFile = new NormalizedMultipartFile(file);
            S3Valid.parentIsValid(PathUtil.getParentPath(Objects.requireNonNull(normalizedFile.getOriginalFilename())));
            S3Valid.fileNameIsValid(PathUtil.getName(normalizedFile.getOriginalFilename()));

            log.debug("Uploading file. userDirectory={}, path={}, filename={}",
                    userDirectory, path, normalizedFile.getOriginalFilename());

            result.add(fileService.putFile(userDirectory, path, normalizedFile));
        }

        log.info("Upload finished. userDirectory={}, uploadedFiles={}", userDirectory, result.size());
        return result;
    }


    public List<ObjectResponseDto> searchObjects(Long id, String name) {

        String userDirectory = String.format("user-%s-files", id);

        log.info("Search object in storage. userDirectory={}, path={}", userDirectory, name);

        S3Valid.fileNameIsValid(name);

        List<ObjectResponseDto> info = fileService.searchObjects(userDirectory, name);

        log.info("Object in storage was found. userDirectory={}, path={}", userDirectory, name);

        return info;
    }


    public ObjectResponseDto moveObject(Long id, String from, String to) {

        String userDirectory = String.format("user-%s-files", id);

        log.info("Moving object. userDirectory={}, from={}, to={}", userDirectory, from, to);

        // file move/rename
        if (MinioFileService.isFile(from) && MinioFileService.isFile(to)) {

            S3Valid.equals(from, to);
            S3Valid.parentIsValid(PathUtil.getParentPath(from)); // fromPath
            S3Valid.parentIsValid(PathUtil.getParentPath(to)); // toPath
            S3Valid.fileNameIsValid(PathUtil.getName(from)); // fromName
            S3Valid.fileNameIsValid(PathUtil.getName(to)); // toName

            fileService.moveFile(userDirectory, from, to);

            log.info("Move file completed. userDirectory={}, from={}, to={}", userDirectory, from, to);

            return fileService.getFileInfo(userDirectory, to);
        }

        // directory move/rename
        if (MinioDirectoryService.isDirectory(from) && MinioDirectoryService.isDirectory(to)) {
            S3Valid.equals(from, to);
            S3Valid.parentIsValid(from);
            S3Valid.parentIsValid(to);

            directoryService.moveDirectory(userDirectory, from, to);

            log.info("Move directory completed. userDirectory={}, from={}, to={}", userDirectory, from, to);

            return new ObjectResponseDto(
                    PathUtil.getParentPath(to),
                    PathUtil.getName(to),
                    Type.DIRECTORY);
        }

        throw new IllegalArgumentException("Invalid move operation from=" + from + ", to=" + to);
    }


    public List<ObjectResponseDto> getDirectoryInfo(Long id, String path) {

        String userDirectory = String.format("user-%s-files", id);

        S3Valid.parentIsValid(path);

        return directoryService.getDirectoryInfo(userDirectory, path);
    }


    public ObjectResponseDto createDirectory(Long id, String path) {

        S3Valid.isBlank(path);
        S3Valid.parentIsValid(path);

        String userDirectory = String.format("user-%s-files", id);

        return directoryService.createDirectory(userDirectory, path);
    }


    public void deleteObject(Long id, String path) {

        String userDirectory = String.format("user-%s-files", id);

        log.info("Deleting object from storage. userDirectory={}, path={}", userDirectory, path);

        S3Valid.isBlank(path);
        S3Valid.fileNameIsValid(path);

        if (MinioDirectoryService.isDirectory(path)) {
            S3Valid.parentIsValid(path);

            directoryService.deleteDirectory(userDirectory, path);

            log.info("Directory deleted. userDirectory={}, path={}", userDirectory, path);
            return;
        }

        fileService.deleteFile(userDirectory, path);
        log.info("File deleted. userDirectory={}, path={}", userDirectory, path);
    }
}