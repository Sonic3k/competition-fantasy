package com.fantasy.competition.repository;

import com.fantasy.competition.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SeasonRepository extends JpaRepository<Season, UUID> {
    List<Season> findByCompetitionId(UUID competitionId);
}
