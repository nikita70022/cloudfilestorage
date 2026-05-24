package com.gigabiba.cloudfilestorage.storage.service;

import com.gigabiba.cloudfilestorage.storage.dto.ObjectResponseDto;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

public interface StorageService {
    ObjectResponseDto getFileInfo(Long id, String path);

    StreamingResponseBody downloadObject(Long id, String path);

    List<ObjectResponseDto> uploadObjects(Long id, String path, List<MultipartFile> files);

    List<ObjectResponseDto> searchObjects(Long id, String name);

    ObjectResponseDto moveObject(Long id, String from, String to);

    List<ObjectResponseDto> getDirectoryInfo(Long id, String path);

    ObjectResponseDto createDirectory(Long id, String path);

    void deleteObject(Long id, String path);
}