package com.fantasy.competition.dto;

import com.fantasy.competition.entity.ImportScript;
import java.time.Instant;
import java.util.UUID;

public record ImportScriptDto(UUID id, String name, String description, String sqlContent,
                               String status, Instant executedAt, String errorMessage,
                               Instant createdAt) {
    public static ImportScriptDto from(ImportScript e) {
        return new ImportScriptDto(e.getId(), e.getName(), e.getDescription(), e.getSqlContent(),
            e.getStatus() != null ? e.getStatus().name() : null,
            e.getExecutedAt(), e.getErrorMessage(), e.getCreatedAt());
    }
}
