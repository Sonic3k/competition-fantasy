package com.fantasy.competition.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/** Lets the admin/viewer app (another Railway domain) call the API. CORS_ORIGINS: comma-separated, or * for any. */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String origins;

    public CorsConfig(AppProperties props) {
        this.origins = props.cors() == null || props.cors().origins() == null ? "*" : props.cors().origins().trim();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var mapping = registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Location")
                .maxAge(3600);
        if ("*".equals(origins)) {
            mapping.allowedOriginPatterns("*");
        } else {
            mapping.allowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new));
        }
    }
}
