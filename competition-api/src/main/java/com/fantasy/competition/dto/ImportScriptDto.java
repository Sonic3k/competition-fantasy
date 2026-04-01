package com.fantasy.competition.dto;

import com.fantasy.competition.entity.ScriptExecution;
import com.fantasy.competition.script.ImportScript;
import java.time.Instant;
import java.util.UUID;

public record ImportScriptDto(String id, String name, String description, String icon,
                               String lastStatus, Instant lastExecutedAt, String lastError) {

    public static ImportScriptDto from(ImportScript script, ScriptExecution lastExec) {
        return new ImportScriptDto(
            script.getId(), script.getName(), script.getDescription(), script.getIcon(),
            lastExec != null ? lastExec.getStatus().name() : null,
            lastExec != null ? lastExec.getExecutedAt() : null,
            lastExec != null ? lastExec.getErrorMessage() : null
        );
    }
}
