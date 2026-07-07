package com.talenteval.talenteval.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class LocalRecordingStorageService implements RecordingStorageService {

    private final Path baseDir;

    public LocalRecordingStorageService(@Value("${app.recordings.dir}") String recordingsDir) {
        this.baseDir = Path.of(recordingsDir);
    }

    @Override
    public String store(byte[] data, Long sessionId, Long questionId) {
        try {
            Path dir = baseDir.resolve(String.valueOf(sessionId));
            Files.createDirectories(dir);
            Path file = dir.resolve(questionId + ".webm");
            Files.write(file, data);
            return baseDir.relativize(file).toString();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store recording", e);
        }
    }

    @Override
    public byte[] load(String storageKey) {
        try {
            return Files.readAllBytes(baseDir.resolve(storageKey));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load recording", e);
        }
    }
}
