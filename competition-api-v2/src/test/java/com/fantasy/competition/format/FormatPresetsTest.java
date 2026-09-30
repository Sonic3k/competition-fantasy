package com.fantasy.competition.format;

import com.fantasy.competition.format.model.FormatDefinition;
import com.fantasy.competition.format.model.StageDef;
import com.fantasy.competition.format.model.StageType;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormatPresetsTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void everyBuiltinPresetIsValidAndNamedAfterItsFile() throws Exception {
        Resource[] files = new PathMatchingResourcePatternResolver().getResources("classpath:presets/*.json");
        assertTrue(files.length >= 20, "expected the preset catalog, found " + files.length);
        for (Resource file : files) {
            try (InputStream in = file.getInputStream()) {
                FormatDefinition def = mapper.readValue(new String(in.readAllBytes(), StandardCharsets.UTF_8), FormatDefinition.class);
                List<String> problems = FormatValidator.validate(def);
                assertTrue(problems.isEmpty(), file.getFilename() + ": " + problems);
                assertEquals(def.key() + ".json", file.getFilename());
            }
        }
    }

    @Test
    void worldCup48ParsesBestThirdsEntry() throws Exception {
        Resource file = new PathMatchingResourcePatternResolver().getResource("classpath:presets/WORLD_CUP_48.json");
        FormatDefinition def = mapper.readValue(file.getContentAsString(StandardCharsets.UTF_8), FormatDefinition.class);
        StageDef ko = def.stage("KO").orElseThrow();
        assertEquals(StageType.KNOCKOUT, ko.type());
        assertEquals(8, ko.entry().bestPlaced().count());
        assertEquals("FIFA_48", ko.entry().bracket());
        assertEquals(6, ko.roundsOrEmpty().size());
    }

    @Test
    void validatorRejectsDanglingEntryAndUnknownTiebreaker() {
        String json = "{\"key\":\"X\",\"stages\":[{\"key\":\"A\",\"type\":\"ROUND_ROBIN\",\"tiebreakers\":[\"NOPE\"]},{\"key\":\"B\",\"type\":\"KNOCKOUT\",\"rounds\":[{\"key\":\"F\"}],\"entry\":{\"from\":\"Z\"}}]}";
        FormatDefinition def = mapper.readValue(json, FormatDefinition.class);
        List<String> problems = FormatValidator.validate(def);
        assertFalse(problems.isEmpty());
        assertTrue(problems.stream().anyMatch(p -> p.contains("NOPE")));
        assertTrue(problems.stream().anyMatch(p -> p.contains("'Z'")));
    }
}
