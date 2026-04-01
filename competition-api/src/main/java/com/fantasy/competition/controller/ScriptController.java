package com.fantasy.competition.controller;

import com.fantasy.competition.dto.ImportScriptDto;
import com.fantasy.competition.entity.ScriptExecution;
import com.fantasy.competition.repository.ImportScriptRepository;
import com.fantasy.competition.script.ImportScript;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scripts")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ScriptController {

    private final List<ImportScript> scripts;
    private final ImportScriptRepository execRepo;

    @GetMapping
    public List<ImportScriptDto> list() {
        return scripts.stream().map(s -> {
            var execs = execRepo.findByNameOrderByCreatedAtDesc(s.getId());
            var last = execs.isEmpty() ? null : execs.get(0);
            return ImportScriptDto.from(s, last);
        }).toList();
    }

    @PostMapping("/{scriptId}/run")
    public ResponseEntity<Map<String, Object>> run(@PathVariable String scriptId) {
        var script = scripts.stream().filter(s -> s.getId().equals(scriptId)).findFirst();
        if (script.isEmpty()) return ResponseEntity.notFound().build();

        var exec = new ScriptExecution();
        exec.setName(scriptId);
        exec.setDescription(script.get().getName());

        try {
            script.get().execute();
            exec.setStatus(ScriptExecution.Status.EXECUTED);
            exec.setExecutedAt(Instant.now());
            execRepo.save(exec);
            return ResponseEntity.ok(Map.of("status", "EXECUTED", "message", "Success"));
        } catch (Exception e) {
            exec.setStatus(ScriptExecution.Status.FAILED);
            exec.setExecutedAt(Instant.now());
            exec.setErrorMessage(e.getMessage());
            execRepo.save(exec);
            return ResponseEntity.ok(Map.of("status", "FAILED", "error", e.getMessage()));
        }
    }
}
