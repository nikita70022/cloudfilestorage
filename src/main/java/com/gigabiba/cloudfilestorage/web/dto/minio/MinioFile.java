package com.gigabiba.cloudfilestorage.web.dto.minio;


import lombok.*;


@NoArgsConstructor
public class MinioFile extends MinioObject {
    public MinioFile(String path, String name, String size, Type type) {
        super(path, name, size, type);
    }
}
