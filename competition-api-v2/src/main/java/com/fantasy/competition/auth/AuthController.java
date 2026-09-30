package com.fantasy.competition.auth;

import com.fantasy.competition.common.AppProperties;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record LoginResponse(String token, String username, int expiresInHours) {}

    private final AppProperties props;
    private final JwtService jwt;
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    public AuthController(AppProperties props, JwtService jwt) {
        this.props = props;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        AppProperties.Admin admin = props.admin();
        if (admin == null || admin.username() == null || admin.password() == null || !matches(req, admin)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
        return ResponseEntity.ok(new LoginResponse(jwt.issue(admin.username()), admin.username(), jwt.ttlHours()));
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of("username", authentication.getName(), "roles", authentication.getAuthorities().stream().map(Object::toString).toList());
    }

    private boolean matches(LoginRequest req, AppProperties.Admin admin) {
        boolean userOk = MessageDigest.isEqual(req.username().getBytes(StandardCharsets.UTF_8), admin.username().getBytes(StandardCharsets.UTF_8));
        boolean passOk = admin.password().startsWith("$2")
                ? bcrypt.matches(req.password(), admin.password())
                : MessageDigest.isEqual(req.password().getBytes(StandardCharsets.UTF_8), admin.password().getBytes(StandardCharsets.UTF_8));
        return userOk && passOk;
    }
}
