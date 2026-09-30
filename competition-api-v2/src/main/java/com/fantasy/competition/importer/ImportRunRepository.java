package com.fantasy.competition.importer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportRunRepository extends JpaRepository<ImportRun, Long> {
    List<ImportRun> findTop50ByOrderByStartedAtDesc();
}
