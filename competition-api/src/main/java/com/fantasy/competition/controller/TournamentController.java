package com.fantasy.competition.controller;

import com.fantasy.competition.dto.AdvanceResult;
import com.fantasy.competition.format.FormatPresetRegistry;
import com.fantasy.competition.format.TournamentFormat;
import com.fantasy.competition.service.KnockoutAdvancementService;
import com.fantasy.competition.service.TournamentGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@RequiredArgsConstructor
public class TournamentController {

    private final FormatPresetRegistry presets;
    private final TournamentGeneratorService generator;
    private final KnockoutAdvancementService advancement;

    /** Format presets available for picking when creating a season. */
    @GetMapping("/formats")
    public List<TournamentFormat> formats() {
        return List.copyOf(presets.all());
    }

    /**
     * Build a season's full structure from a format preset.
     * Optional body: ordered list of team IDs to draw into groups
     * (must equal numGroups * teamsPerGroup to generate fixtures).
     */
    @PostMapping("/seasons/{id}/generate")
    public ResponseEntity<?> generate(@PathVariable UUID id,
                                      @RequestParam(required = false) String presetKey,
                                      @RequestBody(required = false) List<UUID> teamIds) {
        try {
            generator.generate(id, presetKey, teamIds);
            return ResponseEntity.ok(Map.of("status", "generated", "presetKey", presetKey == null ? "" : presetKey));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    /** Semi-auto advance: recompute standings, fill knockout slots, propagate winners. */
    @PostMapping("/seasons/{id}/advance")
    public ResponseEntity<AdvanceResult> advance(@PathVariable UUID id) {
        return ResponseEntity.ok(advancement.advance(id));
    }
}
