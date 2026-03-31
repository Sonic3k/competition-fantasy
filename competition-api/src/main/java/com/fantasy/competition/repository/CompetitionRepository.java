package com.fantasy.competition.repository;

import com.fantasy.competition.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CompetitionRepository extends JpaRepository<Competition, UUID> {
    List<Competition> findByUniverseId(UUID universeId);
}
