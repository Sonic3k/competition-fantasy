package com.fantasy.competition.importer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One entity a run created; reverting a run deletes these in reverse order (children cascade in the database). */
@Entity
@Table(name = "import_changes")
@Getter
@Setter
@NoArgsConstructor
public class ImportChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false)
    private Long runId;

    @Column(nullable = false)
    private Integer ordinal;

    @Column(name = "entity_type", nullable = false, length = 32)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 16)
    private String action;

    public ImportChange(Long runId, Integer ordinal, String entityType, Long entityId, String action) {
        this.runId = runId;
        this.ordinal = ordinal;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
    }
}
