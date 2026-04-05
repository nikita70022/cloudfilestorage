package com.gigabiba.cloudfilestorage.storage.util.path;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathUtil {

    private PathUtil() {
        throw new UnsupportedOperationException("Utility class");
    }


    public static String getName(String path) {

        if (path == null || path.isBlank()) {
            return "";
        }

        Path p = Path.of(path).normalize();
        Path fileName = p.getFileName();

        return fileName != null ? fileName.toString() : "";
    }


    public static String getParentPath(String path) {

        if (path.isBlank()) {
            return "";
        }

        Path parent = Paths.get(path).getParent();
        if (parent == null) {
            return "";
        }

        return parent + "/";
    }

    public static String stripUserDirectory(String userDirectory, String fullPath) {

        Path base = Path.of(userDirectory).normalize();
        Path full = Path.of(fullPath).normalize();

        if (!full.startsWith(base)) {
            throw new IllegalArgumentException("Path outside user directory");
        }

        String result = base.relativize(full).toString();

        if (result.isBlank()) {
            return "";
        }

        return result;
    }

}
