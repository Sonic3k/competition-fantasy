package com.fantasy.competition.format;

import com.fantasy.competition.common.BadRequestException;
import com.fantasy.competition.common.Json;
import com.fantasy.competition.common.NotFoundException;
import com.fantasy.competition.format.model.FormatDefinition;
import com.fantasy.competition.format.model.StageDef;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Optional;

/** Presets and format resolution. A season stores a resolved copy of its format, so presets can change later without touching history. */
@Service
public class FormatService {

    private final FormatPresetRepository presets;
    private final Json json;

    public FormatService(FormatPresetRepository presets, Json json) {
        this.presets = presets;
        this.json = json;
    }

    public FormatDefinition parse(String formatJson) {
        FormatDefinition def = json.read(formatJson, FormatDefinition.class);
        if (def == null) throw new BadRequestException("format is empty");
        return def;
    }

    public FormatDefinition parse(JsonNode node) {
        FormatDefinition def = json.convert(node, FormatDefinition.class);
        if (def == null) throw new BadRequestException("format is empty");
        return def;
    }

    public void validateOrThrow(FormatDefinition def) {
        List<String> problems = FormatValidator.validate(def);
        if (!problems.isEmpty()) throw new BadRequestException("Invalid format definition", problems);
    }

    /**
     * Resolves the format text to store on a season: an explicit definition wins, otherwise the preset's definition.
     * Either way the result is validated.
     */
    public String resolveFormatJson(String presetKey, JsonNode explicitFormat) {
        FormatDefinition def;
        if (explicitFormat != null && !explicitFormat.isNull() && !explicitFormat.isEmpty()) {
            def = parse(explicitFormat);
        } else {
            if (presetKey == null || presetKey.isBlank()) throw new BadRequestException("Either preset or format is required");
            FormatPreset preset = presets.findByKey(presetKey).orElseThrow(() -> new NotFoundException("Format preset", presetKey));
            def = parse(preset.getDefinition());
        }
        validateOrThrow(def);
        return json.write(def);
    }

    public Optional<StageDef> stageDef(String formatJson, String stageKey) {
        return parse(formatJson).stage(stageKey);
    }

    // ---- presets ----

    public List<FormatPreset> list() {
        return presets.findAllByOrderByFamilyAscNameAsc();
    }

    public FormatPreset get(String key) {
        return presets.findByKey(key).orElseThrow(() -> new NotFoundException("Format preset", key));
    }

    @Transactional
    public FormatPreset save(String key, String name, String family, JsonNode definition, boolean allowBuiltinUpdate) {
        if (key == null || key.isBlank()) throw new BadRequestException("preset key is required");
        FormatDefinition def = parse(definition);
        validateOrThrow(def);
        FormatPreset preset = presets.findByKey(key).orElseGet(FormatPreset::new);
        if (preset.getId() != null && preset.isBuiltin() && !allowBuiltinUpdate) {
            throw new BadRequestException("Built-in preset '" + key + "' cannot be edited; clone it instead");
        }
        preset.setKey(key);
        preset.setName(name == null || name.isBlank() ? (def.name() == null ? key : def.name()) : name);
        preset.setFamily(family == null ? def.family() : family);
        preset.setDefinition(json.write(def));
        return presets.save(preset);
    }

    @Transactional
    public FormatPreset clone(String sourceKey, String newKey, String newName) {
        FormatPreset source = get(sourceKey);
        if (presets.findByKey(newKey).isPresent()) throw new BadRequestException("Preset key already exists: " + newKey);
        FormatDefinition def = parse(source.getDefinition());
        FormatDefinition copy = new FormatDefinition(newKey, newName == null ? source.getName() + " (copy)" : newName,
                def.family(), def.teams(), def.points(), def.stages());
        FormatPreset preset = new FormatPreset();
        preset.setKey(newKey);
        preset.setName(copy.name());
        preset.setFamily(copy.family());
        preset.setDefinition(json.write(copy));
        preset.setBuiltin(false);
        return presets.save(preset);
    }

    @Transactional
    public void delete(String key) {
        FormatPreset preset = get(key);
        if (preset.isBuiltin()) throw new BadRequestException("Built-in preset cannot be deleted");
        presets.delete(preset);
    }

    /** Upsert of a built-in preset from the classpath. */
    @Transactional
    public void upsertBuiltin(FormatDefinition def) {
        validateOrThrow(def);
        FormatPreset preset = presets.findByKey(def.key()).orElseGet(FormatPreset::new);
        preset.setKey(def.key());
        preset.setName(def.name() == null ? def.key() : def.name());
        preset.setFamily(def.family());
        preset.setDefinition(json.write(def));
        preset.setBuiltin(true);
        presets.save(preset);
    }
}
