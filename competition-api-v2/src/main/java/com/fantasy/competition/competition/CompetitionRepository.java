package com.fantasy.competition.competition;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    List<Competition> findByUniverseIdOrderByTierAscNameAsc(Long universeId);
    Optional<Competition> findByUniverseIdAndKey(Long universeId, String key);
}
