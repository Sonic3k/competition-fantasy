package com.fantasy.competition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "import_scripts")
@Getter
@Setter
@NoArgsConstructor
public class ImportScript extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "sql_content", nullable = false, columnDefinition = "TEXT")
    private String sqlContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScriptStatus status = ScriptStatus.PENDING;

    private Instant executedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    public enum ScriptStatus {
        PENDING, EXECUTED, FAILED
    }
}
