package com.fantasy.competition.auth;

import com.fantasy.competition.common.AppProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Minimal HS256 JWT for the single admin account. JDK only, no extra dependency. */
@Component
public class JwtService {

    private static final Pattern EXP = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");
    private static final Pattern SUB = Pattern.compile("\"sub\"\\s*:\\s*\"([^\"]*)\"");
    private static final String HEADER = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

    private final byte[] secret;
    private final int ttlHours;

    public JwtService(AppProperties props) {
        String s = props.jwt() == null || props.jwt().secret() == null ? "dev-only-secret" : props.jwt().secret();
        this.secret = s.getBytes(StandardCharsets.UTF_8);
        this.ttlHours = props.jwt() == null ? 72 : props.jwt().ttlHoursOrDefault();
    }

    public int ttlHours() { return ttlHours; }

    public String issue(String subject) {
        long now = Instant.now().getEpochSecond();
        String payload = "{\"sub\":\"" + subject.replace("\"", "") + "\",\"iat\":" + now + ",\"exp\":" + (now + ttlHours * 3600L) + "}";
        String body = HEADER + "." + b64(payload.getBytes(StandardCharsets.UTF_8));
        return body + "." + sign(body);
    }

    /** Returns the subject when the token is well-formed, signed by us and not expired. */
    public Optional<String> verify(String token) {
        if (token == null) return Optional.empty();
        String[] parts = token.split("\\.");
        if (parts.length != 3) return Optional.empty();
        String expected = sign(parts[0] + "." + parts[1]);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        }
        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        Matcher exp = EXP.matcher(payload);
        if (!exp.find() || Long.parseLong(exp.group(1)) < Instant.now().getEpochSecond()) return Optional.empty();
        Matcher sub = SUB.matcher(payload);
        return sub.find() ? Optional.of(sub.group(1)) : Optional.empty();
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return b64(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC unavailable", e);
        }
    }

    private static String b64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
