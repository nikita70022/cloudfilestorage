package com.gigabiba.cloudfilestorage.storage.minio.client;

import com.gigabiba.cloudfilestorage.exception.storage.ResourceExistsException;
import com.gigabiba.cloudfilestorage.exception.storage.ResourceNotExistsException;
import com.gigabiba.cloudfilestorage.storage.minio.properties.MinioProperties;
import com.gigabiba.cloudfilestorage.storage.model.Type;
import com.gigabiba.cloudfilestorage.storage.util.path.PathUtil;
import com.gigabiba.cloudfilestorage.storage.model.DirectoryResponseDto;
import com.gigabiba.cloudfilestorage.storage.model.FileResponseDto;
import com.gigabiba.cloudfilestorage.storage.model.ObjectResponseDto;
import com.gigabiba.cloudfilestorage.exception.storage.StorageException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
@Slf4j
class MinioDirectoryService {

    private final String bucketName;
    private final MinioClient minioClient;

    MinioDirectoryService(MinioClient minioClient, MinioProperties props) {
        this.minioClient = minioClient;
        this.bucketName = props.bucketName();
    }

    protected static boolean isDirectory(String path) {
        return path.endsWith("/") || path.endsWith("__XLDIR__");
    }

    protected ArrayList<ObjectResponseDto> getDirectoryInfo(String userDirectory, String path) {

        if (!pathExists(userDirectory, path)) {
            throw new ResourceNotExistsException("Directory not exist: " + path);
        }

        ArrayList<ObjectResponseDto> info = new ArrayList<>();

        Iterable<Result<Item>> items = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(userDirectory + "/" + path)
                        .recursive(false)
                        .delimiter("/")
                        .build()
        );

        try {
            for (Result<Item> i : items) {
                Item item = i.get();

                if ( (!item.isDir()) && (item.size() == 0) ) {
                    continue;
                }

                ObjectResponseDto object = toObjectResponseDto(userDirectory, item);

                info.add(object);
            }
        } catch (Exception e) {

            log.error("Unexpected error while getting info about directory. userDirectory={}, path={}",
                    userDirectory, path, e);

            throw new StorageException("Failed to get directory info ", e);
        }

        return info;
    }


    protected StreamingResponseBody getDirectory(String userDirectory, String path) {

        if (!pathExists(userDirectory, path)) {
            throw new ResourceNotExistsException("Directory not exists: " + path);
        }

        return outputStream -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {

                Iterable<Result<Item>> items = minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(bucketName)
                                .prefix(userDirectory + "/" + path)
                                .recursive(true)
                                .build()
                );

                for (Result<Item> i : items) {
                    Item item = i.get();

                    if ( (!item.isDir()) && (item.size() == 0) ) {
                        continue;
                    }

                    try (InputStream is = minioClient.getObject(
                            GetObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(item.objectName())
                                    .build()
                    )) {
                        zipOut.putNextEntry(
                                new ZipEntry(PathUtil.getName(
                                        PathUtil.stripUserDirectory(userDirectory, item.objectName()))
                                )
                        );

                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            zipOut.write(buffer, 0, len);
                        }

                        zipOut.closeEntry();
                    }
                }

                zipOut.finish();

            } catch (Exception e) {

                log.error("Unexpected error while get directory. userDirectory={}, path={}", userDirectory, path, e);

                throw new StorageException("Get directory failed: ", e);
            }
        };
    }


    protected DirectoryResponseDto createDirectory(String userDirectory, String pathName) {

        if (pathExists(userDirectory, pathName)) {
            throw new ResourceExistsException("Directory already exists: " + pathName);
        }
        if (!pathExists(userDirectory, PathUtil.getParentPath(pathName))) {
            throw new ResourceNotExistsException("Directory not exists: " + PathUtil.getParentPath(pathName));
        }

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(userDirectory + "/" + pathName)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {

            log.error("Unexpected error while create directory. userDirectory={}, path={}", userDirectory, pathName, e);

            throw new StorageException("Failed to create a directory ", e);
        }

        return new DirectoryResponseDto(
                PathUtil.getParentPath(pathName),
                PathUtil.getName(pathName),
                Type.DIRECTORY);
    }


    protected void deleteDirectory(String userDirectory, String path) {

        if (!path.endsWith("/")) {
            throw new IllegalArgumentException("Path not a directory");
        }

        if (userDirectory.equals(path)) {
            throw new IllegalArgumentException("Cannot delete root directory");
        }

        if (!pathExists(userDirectory, path)) {
            throw new ResourceNotExistsException("Directory not exists: " + path);
        }

        Iterable<Result<Item>> items =
                minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(bucketName)
                                .prefix(userDirectory + "/" + path)
                                .recursive(false)
                                .build());
        try {
            List<DeleteObject> toDelete = new ArrayList<>();
            for (Result<Item> i : items) {
                toDelete.add(new DeleteObject(i.get().objectName()));
            }

            Iterable<Result<DeleteError>> errors =
                    minioClient.removeObjects(
                            RemoveObjectsArgs.builder()
                                    .bucket(bucketName)
                                    .objects(toDelete)
                                    .build());

            for (Result<DeleteError> error : errors) {
                if (error.get() != null) {
                    log.error(error.get().toString());
                }
            }
        } catch (Exception e) {

            log.error("Unexpected error while delete directory. userDirectory={}, path={}", userDirectory, path, e);

            throw new StorageException("Failed to delete directory ", e);
        }
    }


    protected void moveDirectory(String userDirectory, String oldPathName, String newPathName) {

        if (oldPathName.equals(newPathName)) {
            throw new IllegalStateException("Source and destination are the same");
        }
        if (newPathName.startsWith(oldPathName)) {
            throw new IllegalStateException("Cannot move directory inside itself");
        }
        if (!pathExists(userDirectory, oldPathName)) {
            throw new ResourceNotExistsException("Directory not exists: " + oldPathName);
        }
        if (pathExists(userDirectory, newPathName)) {
            throw new ResourceExistsException("Directory already exists: " + newPathName);
        }

        Iterable<Result<Item>> items = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(userDirectory + "/" + oldPathName)
                        .recursive(true)
                        .build()
        );

        List<String> oldObjectNames = new ArrayList<>();
        List<String> copiedObjectNames = new ArrayList<>();

        try {
            for (Result<Item> i : items) {
                oldObjectNames.add(i.get().objectName());
            }

            oldObjectNames.parallelStream().forEach(oldObject -> {
                String newObject = newPathName + oldObject.substring(
                        (userDirectory.length() + 1) + oldPathName.length()
                );

                try {
                    minioClient.copyObject(
                            CopyObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(userDirectory + "/" + newObject)
                                    .source(
                                            CopySource.builder()
                                                    .bucket(bucketName)
                                                    .object(oldObject)
                                                    .build()
                                    )
                                    .build()
                    );

                    copiedObjectNames.add(newObject);

                } catch (Exception e) {

                    List<DeleteObject> rollback = copiedObjectNames.stream()
                            .map(DeleteObject::new)
                            .toList();

                    minioClient.removeObjects(
                            RemoveObjectsArgs.builder()
                                    .bucket(bucketName)
                                    .objects(rollback)
                                    .build()
                    );

                    throw new IllegalStateException("Move failed, rollback executed", e);
                }
            });

            List<DeleteObject> toDelete = new ArrayList<>();
            for (String name : oldObjectNames) {
                toDelete.add(new DeleteObject(name));
            }

            Iterable<Result<DeleteError>> errors =
                    minioClient.removeObjects(
                            RemoveObjectsArgs.builder()
                                    .bucket(bucketName)
                                    .objects(toDelete)
                                    .build());

            for (Result<DeleteError> error : errors) {
                try {
                    DeleteError err = error.get();
                    if (err != null) {
                        log.error(error.get().toString());
                    }

                } catch (Exception e) {
                    log.error("While deleting object was error={}", error.toString(), e);
                }
            }

        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception e) {

            log.error("Unexpected error while move directory. userDirectory={}, oldPathName={}, newPathName={}",
                    userDirectory, oldPathName, newPathName, e);

            throw new StorageException("Failed to move/rename directory ", e);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected boolean pathExists(String userDirectory, String path) {

        if (path.isBlank()) {
            return true;
        }

        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(userDirectory + "/" + path)
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

            log.error("Unexpected error while checking the path of directory. userDirectory={}, path={}",
                    userDirectory, path, e);

            throw new StorageException(e.getMessage(), e);
        }
    }


    private ObjectResponseDto toObjectResponseDto(String userDirectory, Item item) {

        String stripDirectory = PathUtil.stripUserDirectory(userDirectory + "/", item.objectName());
        String parentPath = PathUtil.getParentPath(stripDirectory);
        String objectName = PathUtil.getName(stripDirectory);

        if (item.isDir()) {
            return new DirectoryResponseDto(
                    parentPath,
                    objectName + "/",
                    Type.DIRECTORY);
        }

        return new FileResponseDto(
                parentPath,
                objectName,
                item.size(),
                Type.FILE);
    }


    protected void createDirectoriesIfNotExist(String userDirectory, String path) {

        if (path == null || path.isBlank()) {
            return;
        }

        String[] directories = path.split("/");
        StringBuilder dir = new StringBuilder();

        for (String name : directories) {
            dir.append(name).append("/");

            String current = dir.toString();

            if (!pathExists(userDirectory, current)) {
                createDir(userDirectory, current);
            }
        }
    }



    private void createDir(String userDirectory, String path) {

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(userDirectory + "/" + path)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());

        } catch (Exception e) {

            log.error("Unexpected error while create new folder(file with 0 bytes). userDirectory={}, path={}",
                    userDirectory, path, e);

            throw new StorageException(e.getMessage(), e);
        }
    }
}

