package com.fantasy.competition.competition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Collection;
import java.util.List;

public interface HonourRepository extends JpaRepository<Honour, Long> {
    List<Honour> findBySeasonId(Long seasonId);
    List<Honour> findBySeasonIdIn(Collection<Long> seasonIds);
    List<Honour> findByTeamId(Long teamId);
    @Modifying
    void deleteBySeasonId(Long seasonId);
}
