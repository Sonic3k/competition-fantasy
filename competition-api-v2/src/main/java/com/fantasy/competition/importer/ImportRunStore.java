package com.fantasy.competition.importer;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Persists import_runs in their own transaction so the row survives a rolled-back (dry or failed) import. */
@Component
public class ImportRunStore {

    private final ImportRunRepository runs;

    public ImportRunStore(ImportRunRepository runs) {
        this.runs = runs;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportRun start(String fileName, ImportRun.Source source, String checksum, boolean dryRun) {
        ImportRun run = new ImportRun();
        run.setFileName(fileName);
        run.setSource(source);
        run.setChecksum(checksum);
        run.setDryRun(dryRun);
        run.setStatus(ImportRun.Status.RUNNING);
        run.setStartedAt(Instant.now());
        return runs.save(run);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportRun finish(Long runId, ImportRun.Status status, String summaryJson, String log) {
        ImportRun run = runs.findById(runId).orElseThrow();
        run.setStatus(status);
        run.setSummary(summaryJson);
        run.setLog(log);
        run.setFinishedAt(Instant.now());
        return runs.save(run);
    }
}
