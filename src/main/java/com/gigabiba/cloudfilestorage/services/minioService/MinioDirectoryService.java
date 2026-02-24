package com.gigabiba.cloudfilestorage.services.minioService;

import com.gigabiba.cloudfilestorage.web.exceptions.customExceptions.ResourceExistException;
import com.gigabiba.cloudfilestorage.web.exceptions.customExceptions.ResourceNotFoundException;
import com.gigabiba.cloudfilestorage.utils.MyPath;
import com.gigabiba.cloudfilestorage.web.dto.minio.MinioDirectory;
import com.gigabiba.cloudfilestorage.web.dto.minio.MinioFile;
import com.gigabiba.cloudfilestorage.web.dto.minio.MinioObject;
import com.gigabiba.cloudfilestorage.web.dto.minio.Type;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.*;
import lombok.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
class MinioDirectoryService {

    private final MinioClient minioClient;

    @Autowired
    public MinioDirectoryService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @SneakyThrows
    protected StreamingResponseBody getDirectory(String username, String path) {
        StreamingResponseBody stream = null;
        parentNotExist(username, path);
        stream = outputStream -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                Iterable<Result<Item>> results = minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(username)
                                .prefix(path)
                                .recursive(false)
                                .build()
                );

                for (Result<Item> result : results) {
                    if (!result.get().isDir()) {
                        if (String.valueOf(result.get().size()).getBytes()[0] == 48) {
                            continue;
                        }

                        try (InputStream is = minioClient.getObject(
                                GetObjectArgs.builder()
                                        .bucket(username)
                                        .object(result.get().objectName())
                                        .build()
                        )) {
                            zipOut.putNextEntry(new ZipEntry(MyPath.getName(result.get().objectName())));

                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = is.read(buffer)) != -1) {
                                zipOut.write(buffer, 0, len);
                            }

                            zipOut.closeEntry();
                        }

                    } else {
                        getDirectory(username, result.get().objectName());
                    }

                    zipOut.finish();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        return stream;
    }


    @SneakyThrows
    protected ArrayList<MinioObject> getDirectoryInfo(String username, String path) {
        ArrayList<MinioObject> info = new ArrayList<>();
        boolean bucketFound =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(username).build());
        if (bucketFound) {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(username)
                            .prefix(path)
                            .recursive(false)
                            .build()
            );

            for (Result<Item> result : results) {
                MinioObject object = toMinioObjectInfo(result.get());
                if (object instanceof MinioFile) {
                    if (object.getPath().equalsIgnoreCase(MyPath.getParent(path))) {
                        if (object.getName().equalsIgnoreCase(Paths.get(path).getName(Paths.get(path).getNameCount() - 1).toString())) {
                            continue;
                        }
                    }
                    if (object.getName().endsWith(".ini")) {
                        continue;
                    }
                }
                info.add(object);
            }
        }
        return info;
    }


    @SneakyThrows
    protected MinioDirectory createDirectory(String username, String path) throws IllegalStateException {
        boolean bucketFound =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(username).build());
        if (bucketFound) {
            parentAlreadyExist(username, path);
            parentNotExist(username, MyPath.getParent(path));

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(username)
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        }

        return new MinioDirectory(
                MyPath.getParent(path), //path
                MyPath.getName(path),   //name
                Type.DIRECTORY);        //type
    }

    @SneakyThrows
    protected void copyDirectory(String username, String oldPathName, String newPathName) throws IllegalStateException {
        boolean bucketFound =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(username).build());
        if (bucketFound) {
            parentNotExist(username, oldPathName);
            parentAlreadyExist(username, newPathName);
            parentNotExist(username, MyPath.getParent(newPathName));

            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(username)
                            .object(newPathName)
                            .source(
                                    CopySource.builder()
                                            .bucket(username)
                                            .object(oldPathName)
                                            .build()
                            )
                            .build()
            );
        }
    }

    @SneakyThrows
    protected void deleteDirectory(String username, String path) throws IllegalStateException {
        boolean bucketFound =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(username).build());
        if (bucketFound) {
            parentNotExist(username, path);

            Iterable<Result<Item>> objects =
                    minioClient.listObjects(
                            ListObjectsArgs.builder()
                                    .bucket(username)
                                    .prefix(path)
                                    .recursive(false)
                                    .build());

            List<DeleteObject> toDelete = new ArrayList<>();
            for (Result<Item> item : objects) {
                toDelete.add(new DeleteObject(item.get().objectName()));
            }

            Iterable<Result<DeleteError>> errors =
                    minioClient.removeObjects(
                            RemoveObjectsArgs.builder()
                                    .bucket(username)
                                    .objects(toDelete)
                                    .build());

            for (Result<DeleteError> error : errors) {
                if (error.get() != null) {
                    System.out.println(error.get());
                }
            }
        }
    }

    /// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @SneakyThrows
    protected void parentAlreadyExist(String username, String path) throws ResourceExistException {

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
            throw new ResourceExistException("the directory already exists " + path);
        }
    }

    @SneakyThrows
    protected void parentNotExist(String username, String path) throws ResourceNotFoundException {

        if (path.isBlank()) {
            return;
        }

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
            throw new ResourceNotFoundException(path);
        }
    }

    @SneakyThrows
    protected MinioObject toMinioObjectInfo(Item item) {

        if (item.isDir()) {
            return new MinioDirectory(
                    MyPath.getParent(item.objectName()),     // path
                    MyPath.getName(item.objectName()) + "/", // name
                    Type.directory(item.isDir()));           // type
        }

        return new MinioFile(
                MyPath.getParent(item.objectName()),                          // path
                MyPath.getName(item.objectName()),                            // name
                String.valueOf(((double) item.size()/1024)),                  // size
                Type.directory(item.isDir()));                                // type
    }

    @SneakyThrows
    protected void createDirectoriesIfNotExist(String username, String path) {

        if (path.isBlank()) {
            return;
        }

        String[] directories = path.split("/");

        int i = 0;
        String dir = "";
        do {

            String name = directories[i];
            i++;
            dir = dir + name + "/";

            if (dirExist(username, dir)) {
                continue;
            }

            createDir(username, dir);

        } while (directories.length > i);
    }

    @SneakyThrows
    private boolean dirExist(String username, String name) {
        boolean found = false;
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(username)
                            .object(name)
                            .build()
            );
            found = true;
        } catch (ErrorResponseException e) {
            if (!"NoSuchKey".equals(e.errorResponse().code())) {
                throw e;
            }
        }
        return found;
    }


    @SneakyThrows
    private void createDir(String username, String path) throws IllegalStateException {
        boolean bucketFound =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(username).build());
        if (bucketFound) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(username)
                    .object(path).stream(
                            new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        }
    }
}

