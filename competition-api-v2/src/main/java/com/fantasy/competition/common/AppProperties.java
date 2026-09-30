package com.fantasy.competition.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** All app.* settings (see application.yml). Bound from environment variables on Railway. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Admin admin, Jwt jwt, Cors cors, Storage storage, Imports imports) {

    public record Admin(String username, String password) {}

    public record Jwt(String secret, Integer ttlHours) {
        public int ttlHoursOrDefault() { return ttlHours == null ? 72 : ttlHours; }
    }

    public record Cors(String origins) {}

    public record Storage(String endpoint, String region, String bucket, String keyId, String appKey, String prefix, String cdnBaseUrl) {
        public boolean configured() {
            return keyId != null && !keyId.isBlank() && appKey != null && !appKey.isBlank();
        }
    }

    public record Imports(String dir) {
        public String dirOrDefault() { return dir == null || dir.isBlank() ? "imports" : dir; }
    }
}
