package com.talenteval.talenteval.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class CloudinaryRecordingStorageService implements RecordingStorageService {

    private final Cloudinary cloudinary;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public CloudinaryRecordingStorageService(@Value("${cloudinary.cloud-name}") String cloudName,
                                              @Value("${cloudinary.api-key}") String apiKey,
                                              @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    @Override
    public String store(byte[] data, Long sessionId, Long questionId) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(data, ObjectUtils.asMap(
                    "resource_type", "video",
                    "folder", "talenteval/recordings",
                    "public_id", sessionId + "_" + questionId,
                    "overwrite", true
            ));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to upload recording to Cloudinary", e);
        }
    }

    @Override
    public byte[] load(String storageKey) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(storageKey)).GET().build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.body();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to fetch recording from Cloudinary", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while fetching recording from Cloudinary", e);
        }
    }
}
