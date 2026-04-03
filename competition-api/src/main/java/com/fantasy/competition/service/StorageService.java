package com.fantasy.competition.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.net.URI;

@Service
public class StorageService {

    @Value("${storage.b2.key-id:}") private String keyId;
    @Value("${storage.b2.app-key:}") private String appKey;
    @Value("${storage.b2.bucket:}") private String bucket;
    @Value("${storage.b2.endpoint:}") private String endpoint;
    @Value("${storage.cdn-base:}") private String cdnBase;

    private S3Client s3;

    @PostConstruct
    public void init() {
        if (keyId.isBlank() || appKey.isBlank()) return;
        s3 = S3Client.builder()
            .endpointOverride(URI.create("https://" + endpoint))
            .region(Region.US_EAST_1)
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(keyId, appKey)))
            .forcePathStyleAccessEnabled(true)
            .build();
    }

    public String upload(String path, MultipartFile file) throws Exception {
        if (s3 == null) throw new IllegalStateException("Storage not configured");

        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        s3.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .contentType(contentType)
                .build(),
            RequestBody.fromBytes(file.getBytes())
        );

        return cdnBase.endsWith("/") ? cdnBase + path : cdnBase + "/" + path;
    }
}
