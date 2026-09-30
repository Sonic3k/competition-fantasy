package com.fantasy.competition.importer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportChangeRepository extends JpaRepository<ImportChange, Long> {
    List<ImportChange> findByRunIdOrderByOrdinalDesc(Long runId);
    long countByRunId(Long runId);
}
