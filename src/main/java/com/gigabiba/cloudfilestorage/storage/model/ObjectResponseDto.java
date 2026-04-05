package com.gigabiba.cloudfilestorage.storage.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectResponseDto {
    private String path;
    private String name;
    private Long size;
    private Type type;

    public ObjectResponseDto(String path, String name, Type type) {
        this.path = path;
        this.name = name;
        this.type = type;
    }
}
