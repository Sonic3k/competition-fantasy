package com.fantasy.competition.repository;

import com.fantasy.competition.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {
    List<Match> findByRoundId(UUID roundId);
    List<Match> findByRoundSeasonId(UUID seasonId);
    List<Match> findByRoundStageId(UUID stageId);

    @Query("SELECT m FROM Match m WHERE m.round.stageGroup.id = :stageGroupId ORDER BY m.round.roundNumber")
    List<Match> findByStageGroupId(UUID stageGroupId);
}
