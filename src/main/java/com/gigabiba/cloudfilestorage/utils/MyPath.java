package com.gigabiba.cloudfilestorage.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class MyPath {

    public static String getName(String path) {
        return Paths.get(normalized(path)).getFileName().toString();
    }

    public static String getParent(String path) {
        String normalizedPath = normalized(path);

        if (normalizedPath.isBlank()) {
            return "";
        }

        Path parent = Paths.get(normalizedPath).getParent();
        if (parent == null) {
            return "";
        }

        return parent.toString() + "/";

    }

    public static String normalized(String name) {

        if (name.isBlank()) {
            return "";
        }

        return name.replace(":", "/");
    }
}
