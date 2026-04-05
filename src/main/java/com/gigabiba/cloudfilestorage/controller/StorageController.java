package com.gigabiba.cloudfilestorage.controller;

import com.gigabiba.cloudfilestorage.exception.storage.ValidationException;
import com.gigabiba.cloudfilestorage.openapi.StorageApiDoc;
import com.gigabiba.cloudfilestorage.security.service.UserDetailsImpl;
import com.gigabiba.cloudfilestorage.storage.minio.client.MinioStorageServiceImpl;
import com.gigabiba.cloudfilestorage.storage.model.DirectoryResponseDto;
import com.gigabiba.cloudfilestorage.storage.model.ObjectResponseDto;

import com.gigabiba.cloudfilestorage.storage.service.StorageService;
import org.springframework.http.*;
import org.springframework.security.core.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.security.Principal;
import java.util.*;


@RestController
@RequestMapping("/api")
public class StorageController implements StorageApiDoc {

    private final StorageService storageServiceImpl;

    public StorageController(MinioStorageServiceImpl storageServiceImpl) {
        this.storageServiceImpl = storageServiceImpl;
    }

    @GetMapping(value = "/resource", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ObjectResponseDto> getFileInfo(@RequestParam(value = "path") String path,
                                                         @AuthenticationPrincipal UserDetailsImpl user) {


        ObjectResponseDto fileInfo = storageServiceImpl.getFileInfo(user.getId(), path);

        return ResponseEntity
                .status(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fileInfo);
    }


    @GetMapping(value = "/resource/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> downloadObject(@RequestParam(value = "path") String path,
                                                                @AuthenticationPrincipal UserDetailsImpl user) {

        StreamingResponseBody object = storageServiceImpl.downloadObject(user.getId(), path);

        return ResponseEntity
                .status(200)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(object);
    }


    @PostMapping(value = "/resource", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                                         produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ObjectResponseDto>> uploadObjects(@RequestParam String path,
                                                                 @RequestParam(name = "object") List<MultipartFile> files,
                                                                 @AuthenticationPrincipal UserDetailsImpl user) {

        List<ObjectResponseDto> result = storageServiceImpl.uploadObjects(user.getId(), path, files);

        return ResponseEntity
                .status(201)
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }


    @GetMapping(value = "/resource/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ObjectResponseDto>> searchObject(@RequestParam String query,
                                                                @AuthenticationPrincipal UserDetailsImpl user)
            throws ValidationException {

        List<ObjectResponseDto> result = storageServiceImpl.searchObjects(user.getId(), query);

        return ResponseEntity
                .status(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }


    @GetMapping(value = "/resource/move", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ObjectResponseDto> moveObject(@RequestParam(required = false) String from,
                                                        @RequestParam(required = false) String to,
                                                        @AuthenticationPrincipal UserDetailsImpl user) {

        ObjectResponseDto movedObject = storageServiceImpl.moveObject(user.getId(), from, to);

        return ResponseEntity
                .status(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(movedObject);
    }


    @GetMapping(value = "/directory", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ObjectResponseDto>> getDirectoryInfo(@RequestParam(required = false) String path,
                                                                    @AuthenticationPrincipal UserDetailsImpl user)
            throws ValidationException {

        List<ObjectResponseDto> info = storageServiceImpl.getDirectoryInfo(user.getId(), path);

        return ResponseEntity
                .status(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(info);
    }


    @PostMapping(value = "/directory", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DirectoryResponseDto> createDirectory(@RequestParam String path,
                                                                @AuthenticationPrincipal UserDetailsImpl user)
            throws ValidationException {

        DirectoryResponseDto dirInfo = storageServiceImpl.createDirectory(user.getId(), path);

        return ResponseEntity
                .status(201)
                .contentType(MediaType.APPLICATION_JSON)
                .body(dirInfo);
    }


    @DeleteMapping(value = "/resource", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ObjectResponseDto> deleteObject(@RequestParam String path,
                                                          @AuthenticationPrincipal UserDetailsImpl user)
            throws ValidationException {

        storageServiceImpl.deleteObject(user.getId(), path);

        return ResponseEntity
                .status(204)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .build();
    }
}
