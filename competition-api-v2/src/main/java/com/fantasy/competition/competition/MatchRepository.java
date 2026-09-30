package com.fantasy.competition.competition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findBySeasonId(Long seasonId);
    List<Match> findByRoundId(Long roundId);
    List<Match> findByRoundIdIn(Collection<Long> roundIds);
    List<Match> findByTieIdOrderByLegAsc(Long tieId);
    List<Match> findByTieIdIn(Collection<Long> tieIds);
    List<Match> findByStageId(Long stageId);

    @Query("select m from Match m where m.homeTeamId = :teamId or m.awayTeamId = :teamId order by m.matchDate desc, m.id desc")
    List<Match> findByTeam(@Param("teamId") Long teamId);

    @Query("select m from Match m where (m.homeTeamId = :a and m.awayTeamId = :b) or (m.homeTeamId = :b and m.awayTeamId = :a) order by m.matchDate asc, m.id asc")
    List<Match> findHeadToHead(@Param("a") Long a, @Param("b") Long b);
}
