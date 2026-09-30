package com.fantasy.competition.competition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface StageRepository extends JpaRepository<Stage, Long> {
    List<Stage> findBySeasonIdOrderByOrdinalAsc(Long seasonId);
    Optional<Stage> findBySeasonIdAndKey(Long seasonId, String key);
    @Modifying
    void deleteBySeasonId(Long seasonId);
}
