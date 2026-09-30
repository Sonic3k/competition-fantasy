package com.fantasy.competition.format;

import com.fantasy.competition.common.Json;
import com.fantasy.competition.format.model.FormatDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Loads classpath:presets/*.json into format_presets on startup (built-in presets are upserted, never deleted). */
@Configuration
public class PresetLoader {

    private static final Logger log = LoggerFactory.getLogger(PresetLoader.class);

    @Bean
    ApplicationRunner loadBuiltinPresets(FormatService formats, Json json) {
        return args -> {
            Resource[] files = new PathMatchingResourcePatternResolver().getResources("classpath:presets/*.json");
            int ok = 0;
            for (Resource file : files) {
                try (InputStream in = file.getInputStream()) {
                    String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    FormatDefinition def = json.read(text, FormatDefinition.class);
                    formats.upsertBuiltin(def);
                    ok++;
                } catch (Exception e) {
                    log.error("Preset {} failed to load: {}", file.getFilename(), e.getMessage());
                }
            }
            log.info("Loaded {} built-in format presets", ok);
        };
    }
}
