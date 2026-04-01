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
public class ScriptExecution extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "sql_content", columnDefinition = "TEXT")
    private String sqlContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    private Instant executedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    public enum Status {
        PENDING, EXECUTED, FAILED
    }
}
