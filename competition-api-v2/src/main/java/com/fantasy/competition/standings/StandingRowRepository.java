package com.fantasy.competition.standings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Collection;
import java.util.List;

public interface StandingRowRepository extends JpaRepository<StandingRow, Long> {
    List<StandingRow> findByStandingIdOrderByPositionAsc(Long standingId);
    List<StandingRow> findByStandingIdInOrderByPositionAsc(Collection<Long> standingIds);
    List<StandingRow> findByTeamId(Long teamId);
    @Modifying
    void deleteByStandingId(Long standingId);
}
