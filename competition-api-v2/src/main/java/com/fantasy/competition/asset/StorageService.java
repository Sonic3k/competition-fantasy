package com.fantasy.competition.asset;

import com.fantasy.competition.common.AppProperties;
import com.fantasy.competition.common.BadRequestException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;

/** Backblaze B2 through its S3-compatible API; files are served through the BunnyCDN pull zone. */
@Service
public class StorageService {

    private final AppProperties.Storage cfg;
    private volatile S3Client client;

    public StorageService(AppProperties props) {
        this.cfg = props.storage();
    }

    public boolean configured() {
        return cfg != null && cfg.configured();
    }

    public String prefix() {
        String p = cfg == null || cfg.prefix() == null ? "competition-fantasy/v2" : cfg.prefix();
        return p.endsWith("/") ? p.substring(0, p.length() - 1) : p;
    }

    public String publicUrl(String key) {
        String base = cfg == null || cfg.cdnBaseUrl() == null ? "" : cfg.cdnBaseUrl();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "/" + key;
    }

    public void put(String key, byte[] bytes, String contentType) {
        client().putObject(PutObjectRequest.builder().bucket(cfg.bucket()).key(key).contentType(contentType).build(), RequestBody.fromBytes(bytes));
    }

    public void delete(String key) {
        client().deleteObject(DeleteObjectRequest.builder().bucket(cfg.bucket()).key(key).build());
    }

    private S3Client client() {
        if (!configured()) throw new BadRequestException("Storage is not configured (B2_KEY_ID / B2_APP_KEY)");
        S3Client c = client;
        if (c == null) {
            synchronized (this) {
                if (client == null) {
                    client = S3Client.builder()
                            .endpointOverride(URI.create(cfg.endpoint()))
                            .region(Region.of(cfg.region() == null ? "us-east-005" : cfg.region()))
                            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(cfg.keyId(), cfg.appKey())))
                            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                            .build();
                }
                c = client;
            }
        }
        return c;
    }
}
