package com.fantasy.competition.competition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface SeasonTeamRepository extends JpaRepository<SeasonTeam, Long> {
    List<SeasonTeam> findBySeasonIdOrderBySeedAscIdAsc(Long seasonId);
    Optional<SeasonTeam> findBySeasonIdAndTeamId(Long seasonId, Long teamId);
    @Modifying
    void deleteBySeasonId(Long seasonId);
}
