package com.gigabiba.cloudfilestorage.storage.dto;

public enum Type {
    FILE,
    DIRECTORY;

    public static Type directory(boolean b) {
        return b ? Type.DIRECTORY : Type.FILE;
    }
}