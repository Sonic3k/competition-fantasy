package com.fantasy.competition.repository;

import com.fantasy.competition.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {
    List<Match> findByRoundId(UUID roundId);
    List<Match> findByRoundSeasonId(UUID seasonId);
}
