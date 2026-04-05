package com.gigabiba.cloudfilestorage.storage.model;



public class FileResponseDto extends ObjectResponseDto {
    public FileResponseDto(String path, String name, long size, Type type) {
        super(path, name, size, type);
    }
}
