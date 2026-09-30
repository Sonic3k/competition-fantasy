package com.fantasy.competition.format;

import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import java.util.List;

@RestController
public class PresetController {

    public record PresetDto(Long id, String key, String name, String family, boolean builtin, @JsonRawValue String definition) {
        static PresetDto of(FormatPreset p) {
            return new PresetDto(p.getId(), p.getKey(), p.getName(), p.getFamily(), p.isBuiltin(), p.getDefinition());
        }
    }

    public record PresetRequest(@NotBlank String key, String name, String family, JsonNode definition) {}
    public record CloneRequest(@NotBlank String newKey, String newName) {}

    private final FormatService formats;

    public PresetController(FormatService formats) {
        this.formats = formats;
    }

    @GetMapping("/api/presets")
    public List<PresetDto> list() {
        return formats.list().stream().map(PresetDto::of).toList();
    }

    @GetMapping("/api/presets/{key}")
    public PresetDto get(@PathVariable String key) {
        return PresetDto.of(formats.get(key));
    }

    @PostMapping("/api/admin/presets")
    @ResponseStatus(HttpStatus.CREATED)
    public PresetDto create(@RequestBody PresetRequest req) {
        return PresetDto.of(formats.save(req.key(), req.name(), req.family(), req.definition(), false));
    }

    @PutMapping("/api/admin/presets/{key}")
    public PresetDto update(@PathVariable String key, @RequestBody PresetRequest req) {
        return PresetDto.of(formats.save(key, req.name(), req.family(), req.definition(), false));
    }

    @PostMapping("/api/admin/presets/{key}/clone")
    @ResponseStatus(HttpStatus.CREATED)
    public PresetDto clone(@PathVariable String key, @RequestBody CloneRequest req) {
        return PresetDto.of(formats.clone(key, req.newKey(), req.newName()));
    }

    @DeleteMapping("/api/admin/presets/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String key) {
        formats.delete(key);
    }
}
