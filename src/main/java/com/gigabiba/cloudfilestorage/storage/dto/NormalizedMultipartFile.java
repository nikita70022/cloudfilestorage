package com.gigabiba.cloudfilestorage.storage.dto;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Creates a MultipartFile wrapper that normalizes filenames coming from browsers.
 * <p>
 * Some browsers send Windows-style paths in multipart uploads
 * (e.g. "C:\folder\file.txt"). Since ':' is not allowed in our storage
 * naming scheme, the colon is replaced with '/'.
 * <p>
 * If the original filename is null or empty, an empty name is used.
 */
public class NormalizedMultipartFile implements MultipartFile {

    private final MultipartFile original;
    private final String normalizedFilename;

    public NormalizedMultipartFile(MultipartFile original) {
        this.original = original;
        String filename = original.getOriginalFilename();
        this.normalizedFilename = (filename == null || filename.isEmpty())
                ? ""
                : filename.replace(":", "/");
    }

    @Override
    public String getOriginalFilename() {
        return normalizedFilename;
    }

    @Override
    public String getName() {
        return original.getName();
    }

    @Override
    public String getContentType() {
        return original.getContentType();
    }

    @Override
    public boolean isEmpty() {
        return original.isEmpty();
    }

    @Override
    public long getSize() {
        return original.getSize();
    }

    @Override
    public byte[] getBytes() throws IOException {
        return original.getBytes();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return original.getInputStream();
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        original.transferTo(dest);
    }
}