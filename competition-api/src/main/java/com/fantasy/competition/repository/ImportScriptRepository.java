package com.fantasy.competition.repository;

import com.fantasy.competition.entity.ScriptExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ImportScriptRepository extends JpaRepository<ScriptExecution, UUID> {
    List<ScriptExecution> findByNameOrderByCreatedAtDesc(String name);
}
