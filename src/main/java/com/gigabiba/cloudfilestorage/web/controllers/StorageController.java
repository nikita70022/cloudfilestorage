package com.gigabiba.cloudfilestorage.web.controllers;

import com.gigabiba.cloudfilestorage.services.AuthService;
import com.gigabiba.cloudfilestorage.web.exceptions.customExceptions.ValidationException;
import com.gigabiba.cloudfilestorage.web.dto.minio.MinioDirectory;
import com.gigabiba.cloudfilestorage.services.minioService.*;
import com.gigabiba.cloudfilestorage.web.dto.minio.MinioObject;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.*;


@RestController
@RequestMapping("/api")
public class StorageController {
    @Autowired
    private AuthService authService;
    @Autowired
    private MinioObjectService objectService;


    @GetMapping(
            value = "/resource",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MinioObject> getFileInfo(@RequestParam(value = "path") String path,
                                                   Authentication authentication) {

        authService.isAuthenticated(authentication);
        String username = authentication.getName();
        MinioObject fileInfo = objectService.getFileInfo(username, path);

        return ResponseEntity
                .ok()
                .body(fileInfo);
    }


    @GetMapping(
            value = "/resource/download",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<StreamingResponseBody> downloadObject(@RequestParam(value = "path") String path,
                                                                Authentication authentication) {
        
        authService.isAuthenticated(authentication);
        String username = authentication.getName();

        StreamingResponseBody object = objectService.downloadObject(username, path);

        return ResponseEntity
                .ok()
                .body(object);
    }


    @PostMapping(
            value = "/resource",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<MinioObject>> uploadObject(@RequestParam(required = false) String path,
                                                          @RequestParam(name = "object") List<MultipartFile> files,
                                                          Authentication authentication) {

        authService.isAuthenticated(authentication);
        String username = authentication.getName();

        List<MinioObject> result = objectService.uploadObjects(username, path, files);

        return ResponseEntity
                .status(201)
                .body(result);
    }


    @GetMapping(
            value = "/resource/search",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ArrayList<MinioObject>> searchObject(@RequestParam String query,
                                                               Authentication authentication) throws ValidationException {
        authService.isAuthenticated(authentication);
        String username = authentication.getName();

        ArrayList<MinioObject> result = objectService.searchObjects(username, query);

        return ResponseEntity
                .ok()
                .body(result);
    }


    @GetMapping(
            value = "/api/resource/move",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MinioObject> moveFile(@RequestParam(required = false) String from,
                                                @RequestParam(required = false) String to,
                                                Authentication authentication) {
        authService.isAuthenticated(authentication);
        String username = authentication.getName();

        MinioObject movedObject = objectService.moveObject(username, from, to);

        return ResponseEntity
                .ok()
                .body(movedObject);
    }


    @GetMapping(
            value = "/directory",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ArrayList<MinioObject>> getDirectoryInfo(@RequestParam String path,
                                                                   Authentication authentication) throws ValidationException {
        authService.isAuthenticated(authentication);
        String username = authentication.getName();

        ArrayList<MinioObject> info = objectService.getDirectoryInfo(username, path);

        return ResponseEntity
                .ok()
                .body(info);
    }


    @PostMapping(value = "/directory",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MinioDirectory> createDirectory(@RequestParam String path,
                                                          Authentication authentication) throws ValidationException {
        authService.isAuthenticated(authentication);
        String username = authentication.getName();

        MinioDirectory dirInfo = objectService.createDirectory(username, path);

        return ResponseEntity
                .status(201)
                .body(dirInfo);
    }


    @DeleteMapping(
            value = "/resource",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MinioObject> deleteObject(@RequestParam String path,
                                                    Authentication authentication) throws ValidationException {
        authService.isAuthenticated(authentication);
        String username = authentication.getName();

        objectService.deleteObject(username, path);

        return ResponseEntity
                .noContent()
                .build();


    }
}
