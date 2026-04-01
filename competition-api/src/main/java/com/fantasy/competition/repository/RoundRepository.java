package com.fantasy.competition.repository;

import com.fantasy.competition.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RoundRepository extends JpaRepository<Round, UUID> {
    List<Round> findBySeasonIdOrderByRoundNumber(UUID seasonId);
    List<Round> findByStageIdOrderByRoundNumber(UUID stageId);
    List<Round> findByStageGroupIdOrderByRoundNumber(UUID stageGroupId);
}
