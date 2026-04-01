package com.fantasy.competition.repository;

import com.fantasy.competition.entity.ImportScript;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ImportScriptRepository extends JpaRepository<ImportScript, UUID> {
    List<ImportScript> findAllByOrderByCreatedAtDesc();
}
