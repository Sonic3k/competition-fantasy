package com.fantasy.competition.controller;

import com.fantasy.competition.dto.ImportScriptDto;
import com.fantasy.competition.entity.ImportScript;
import com.fantasy.competition.repository.ImportScriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/scripts")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ScriptController {

    private final ImportScriptRepository repo;
    private final DataSource dataSource;

    @GetMapping
    public List<ImportScriptDto> list() {
        return repo.findAllByOrderByCreatedAtDesc().stream().map(ImportScriptDto::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImportScriptDto> get(@PathVariable UUID id) {
        return repo.findById(id).map(e -> ResponseEntity.ok(ImportScriptDto.from(e))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ImportScriptDto create(@RequestBody ImportScript script) {
        script.setStatus(ImportScript.ScriptStatus.PENDING);
        return ImportScriptDto.from(repo.save(script));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<ImportScriptDto> execute(@PathVariable UUID id) {
        return repo.findById(id).map(script -> {
            if (script.getStatus() == ImportScript.ScriptStatus.EXECUTED) {
                return ResponseEntity.badRequest().body(ImportScriptDto.from(script));
            }
            try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
                conn.setAutoCommit(false);
                stmt.execute(script.getSqlContent());
                conn.commit();
                script.setStatus(ImportScript.ScriptStatus.EXECUTED);
                script.setExecutedAt(Instant.now());
                script.setErrorMessage(null);
            } catch (Exception e) {
                script.setStatus(ImportScript.ScriptStatus.FAILED);
                script.setErrorMessage(e.getMessage());
            }
            return ResponseEntity.ok(ImportScriptDto.from(repo.save(script)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return repo.findById(id).map(script -> {
            if (script.getStatus() == ImportScript.ScriptStatus.EXECUTED) {
                return ResponseEntity.badRequest().<Void>build();
            }
            repo.deleteById(id);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
