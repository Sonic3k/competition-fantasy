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

    @Query(value = """
        SELECT DISTINCT m.* FROM matches m
        JOIN rounds r ON m.round_id = r.id
        JOIN stage_groups sg ON sg.id = :stageGroupId AND sg.stage_id = r.stage_id
        JOIN stage_group_teams sgt ON sgt.stage_group_id = sg.id
        WHERE m.home_team_id = sgt.team_id OR m.away_team_id = sgt.team_id
        ORDER BY r.round_number
        """, nativeQuery = true)
    List<Match> findByStageGroupTeams(UUID stageGroupId);
}
