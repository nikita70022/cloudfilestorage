package com.gigabiba.cloudfilestorage.web.dto.minio;

public enum Type {
    FILE,
    DIRECTORY;

    public static Type directory(boolean b) {
        return b ? Type.DIRECTORY : Type.FILE;
    }
}
