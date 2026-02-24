package com.gigabiba.cloudfilestorage.web.dto.minio;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class MinioObject {
    String path;
    String name;
    String size;
    Type type;

    public MinioObject(String path, String name, Type type) {
        this.path = path;
        this.name = name;
        this.type = type;
    }

    public MinioObject(String path, String name, String size, Type type) {
        this.path = path;
        this.name = name;
        this.size = size;
        this.type = type;
    }
}
