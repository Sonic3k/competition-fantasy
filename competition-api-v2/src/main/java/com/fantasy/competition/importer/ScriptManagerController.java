package com.fantasy.competition.importer;

import com.fantasy.competition.common.BadRequestException;
import com.fantasy.competition.common.NotFoundException;
import com.fasterxml.jackson.annotation.JsonRawValue;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Script Manager: JSON files committed under competition-api-v2/imports (see {@link ImportFiles}) plus uploads.
 * schema/ and examples/ subfolders are listed but flagged so they are not run by accident.
 */
@RestController
@RequestMapping("/api/admin/imports")
public class ScriptManagerController {

    public record FileInfo(String path, long sizeBytes, Instant modifiedAt, boolean example) {}
    public record RunRequest(String file, Boolean dryRun) {}
    public record RunDto(Long id, String fileName, String source, String checksum, boolean dryRun, String status,
                         Instant startedAt, Instant finishedAt, @JsonRawValue String summary, String log, long changes) {}

    private final ImportService imports;
    private final ImportRunRepository runs;
    private final ImportChangeRepository changes;
    private final ImportFiles files;

    public ScriptManagerController(ImportService imports, ImportRunRepository runs, ImportChangeRepository changes, ImportFiles files) {
        this.imports = imports;
        this.runs = runs;
        this.changes = changes;
        this.files = files;
    }

    @GetMapping("/files")
    public List<FileInfo> files() throws IOException {
        return files.list().stream()
                .map(e -> new FileInfo(e.path(), e.sizeBytes(), e.modifiedAt(), e.path().startsWith("schema/") || e.path().startsWith("examples/")))
                .toList();
    }

    @GetMapping(value = "/files/content", produces = MediaType.APPLICATION_JSON_VALUE)
    public String content(@RequestParam String path) throws IOException {
        return files.read(path);
    }

    @PostMapping("/run")
    public ImportService.Report run(@RequestBody RunRequest req) throws IOException {
        if (req.file() == null || req.file().isBlank()) throw new BadRequestException("file is required");
        String text = files.read(req.file());
        return imports.run(req.file(), text, ImportRun.Source.REPO, req.dryRun() != null && req.dryRun());
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportService.Report upload(@RequestPart("file") MultipartFile file,
                                       @RequestParam(defaultValue = "false") boolean dryRun) throws IOException {
        if (file.isEmpty()) throw new BadRequestException("file is empty");
        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        String name = file.getOriginalFilename() == null ? "upload.json" : file.getOriginalFilename();
        return imports.run(name, text, ImportRun.Source.UPLOAD, dryRun);
    }

    @GetMapping("/runs")
    public List<RunDto> runs() {
        return runs.findTop50ByOrderByStartedAtDesc().stream().map(this::toDto).toList();
    }

    @GetMapping("/runs/{id}")
    public RunDto run(@PathVariable Long id) {
        return toDto(runs.findById(id).orElseThrow(() -> new NotFoundException("Import run", id)));
    }

    @PostMapping("/runs/{id}/revert")
    public RunDto revert(@PathVariable Long id) {
        ImportRun run = runs.findById(id).orElseThrow(() -> new NotFoundException("Import run", id));
        if (run.getStatus() != ImportRun.Status.SUCCESS || run.isDryRun()) throw new BadRequestException("Only a successful real run can be reverted");
        imports.revert(id);
        return run(id);
    }

    @GetMapping(value = "/schema", produces = MediaType.APPLICATION_JSON_VALUE)
    public String schema() throws IOException {
        return files.read("schema/v1.json");
    }

    private RunDto toDto(ImportRun r) {
        return new RunDto(r.getId(), r.getFileName(), r.getSource().name(), r.getChecksum(), r.isDryRun(), r.getStatus().name(),
                r.getStartedAt(), r.getFinishedAt(), r.getSummary(), r.getLog(), changes.countByRunId(r.getId()));
    }
}
