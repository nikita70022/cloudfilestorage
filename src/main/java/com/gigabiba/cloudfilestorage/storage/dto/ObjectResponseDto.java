package com.gigabiba.cloudfilestorage.storage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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