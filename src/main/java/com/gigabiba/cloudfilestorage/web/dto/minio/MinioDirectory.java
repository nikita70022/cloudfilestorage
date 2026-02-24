package com.gigabiba.cloudfilestorage.web.dto.minio;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class MinioDirectory extends MinioObject {
    public MinioDirectory(String path, String name, Type type) {
        super(path, name, type);
    }
}
