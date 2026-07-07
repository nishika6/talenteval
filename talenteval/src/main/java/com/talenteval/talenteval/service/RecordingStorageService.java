package com.talenteval.talenteval.service;

public interface RecordingStorageService {

    String store(byte[] data, Long sessionId, Long questionId);

    byte[] load(String storageKey);
}
