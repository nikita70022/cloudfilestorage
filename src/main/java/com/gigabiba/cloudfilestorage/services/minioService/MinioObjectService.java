package com.gigabiba.cloudfilestorage.services.minioService;

import com.gigabiba.cloudfilestorage.utils.MyPath;
import com.gigabiba.cloudfilestorage.utils.minioValidation.S3Valid;
import com.gigabiba.cloudfilestorage.web.dto.minio.MinioDirectory;
import com.gigabiba.cloudfilestorage.web.dto.minio.MinioFile;
import com.gigabiba.cloudfilestorage.web.dto.minio.MinioObject;
import com.gigabiba.cloudfilestorage.web.dto.minio.Type;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.ArrayList;
import java.util.List;

@Service
public class MinioObjectService {

    private final MinioClient minioClient;
    private final MinioBucketService bucketService;
    private final MinioDirectoryService directoryService;
    private final MinioFileService fileService;


    @Autowired
    public MinioObjectService(MinioBucketService bucketService,
                              MinioClient minioClient,
                              MinioDirectoryService directoryService,
                              MinioFileService fileService) {
        this.minioClient = minioClient;
        this.bucketService = bucketService;
        this.directoryService = directoryService;
        this.fileService = fileService;
    }

    @SneakyThrows
    public MinioObject getFileInfo(String username, String path) {
        S3Valid.fileNameIsValid(path);
        bucketService.bucketExist(username);
        return fileService.getFileInfo(username, path);
    }


    @SneakyThrows
    public StreamingResponseBody downloadObject(String username, String path) {
        bucketService.bucketExist(username);

        if (S3Valid.isDirectory(path)) {
            S3Valid.parentIsValid(path);
            return directoryService.getDirectory(username, path);
        }

        S3Valid.fileNameIsValid(path);
        return fileService.getFile(username, path);
    }

    @SneakyThrows
    public List<MinioObject> uploadObjects(String username, String path, List<MultipartFile> files) {
        S3Valid.parentIsValid(path);
        bucketService.bucketExist(username);

        List<MinioObject> result = new ArrayList<>();

        for (MultipartFile file : files) {
            S3Valid.parentIsValid(MyPath.getParent(file.getOriginalFilename()));
            S3Valid.fileNameIsValid(MyPath.getName(file.getOriginalFilename()));
            fileService.putFile(username, path, file);
            result.add(fileService.getFileInfo(username, path + MyPath.normalized(file.getOriginalFilename())));
        }

        return result;
    }


    @SneakyThrows
    public ArrayList<MinioObject> searchObjects(String username, String name) {
        S3Valid.fileNameIsValid(name);
        bucketService.bucketExist(username);

        ArrayList<MinioObject> info = new ArrayList<>();

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(username)
                        .prefix(name)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : results) {
            Item item = result.get();
            String[] path = item.objectName().split("/");

            if (S3Valid.isDirectory(item.objectName())) {
                info.add(new MinioDirectory(
                        item.objectName().substring(0, item.objectName().length() - path[path.length - 1].length() - 1), // path
                        path[path.length - 1] + "/", // name
                        Type.DIRECTORY)); // type
                continue;
            }

            info.add(new MinioFile(
                    item.objectName(),                                              // path
                    path[path.length - 1],                                          // name
                    String.valueOf(((double) item.size()/1024)),                  // size
                    Type.directory(item.isDir())));                                 // type
        }

        return info;
    }

    @SneakyThrows
    public MinioObject moveObject(String username, String from, String to) {
        bucketService.bucketExist(username);

        if (S3Valid.isDirectory(from) && S3Valid.isDirectory(to)) {
            S3Valid.equals(from, to);
            S3Valid.parentIsValid(from);
            S3Valid.parentIsValid(to);

            directoryService.copyDirectory(username, from, to);
            directoryService.deleteDirectory(username, from);
            return new MinioDirectory(
                            MyPath.getParent(to),    // path
                            MyPath.getName(to),      // name
                            Type.DIRECTORY);         // type
        }

            S3Valid.equals(from, to);
            S3Valid.fileNameIsValid(from);
            S3Valid.fileNameIsValid(to);

            fileService.copyFile(username, from, to);
            fileService.deleteFile(username, from);
            return fileService.getFileInfo(username,to);
    }

    @SneakyThrows
    public ArrayList<MinioObject> getDirectoryInfo(String username, String path) {
        bucketService.bucketExist(username);
        S3Valid.parentIsValid(path);
        return directoryService.getDirectoryInfo(username, path);
    }

    @SneakyThrows
    public MinioDirectory createDirectory(String username, String path) {
        bucketService.bucketExist(username);
        S3Valid.isBlank(path);
        S3Valid.parentIsValid(path);
        return directoryService.createDirectory(username, path);
    }

    @SneakyThrows
    public void deleteObject(String username, String path) {
        bucketService.bucketExist(username);
        S3Valid.fileNameIsValid(path);
        S3Valid.isBlank(path);

        if (S3Valid.isDirectory(path)) {
            S3Valid.parentIsValid(path);
            directoryService.deleteDirectory(username, path);
            return;
        }

        fileService.deleteFile(username, path);
    }
}

