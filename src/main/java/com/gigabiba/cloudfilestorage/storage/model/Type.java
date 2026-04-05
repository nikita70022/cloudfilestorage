package com.gigabiba.cloudfilestorage.storage.model;

public enum Type {
    FILE,
    DIRECTORY;

    public static Type directory(boolean b) {
        return b ? Type.DIRECTORY : Type.FILE;
    }
}
