package com.fantasy.competition.repository;

import com.fantasy.competition.entity.Standing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StandingRepository extends JpaRepository<Standing, UUID> {
    List<Standing> findBySeasonIdOrderByPointsDescGoalsForDesc(UUID seasonId);
    List<Standing> findBySeasonIdAndTypeOrderByPointsDescGoalsForDesc(UUID seasonId, Standing.StandingType type);
    List<Standing> findBySeasonIdAndTypeAndAfterRoundOrderByPointsDescGoalsForDesc(UUID seasonId, Standing.StandingType type, Integer afterRound);
    List<Standing> findBySeasonIdAndAfterRoundIsNullOrderByPointsDescGoalsForDesc(UUID seasonId);
    List<Standing> findBySeasonIdAndTypeAndAfterRoundIsNullOrderByPointsDescGoalsForDesc(UUID seasonId, Standing.StandingType type);
    List<Standing> findByStageGroupIdOrderByPointsDescGoalsForDesc(UUID stageGroupId);
    List<Standing> findByStageGroupIdAndTypeOrderByPointsDescGoalsForDesc(UUID stageGroupId, Standing.StandingType type);
    void deleteBySeasonIdAndTypeAndAfterRound(UUID seasonId, Standing.StandingType type, Integer afterRound);
    void deleteBySeasonIdAndTypeAndAfterRoundIsNull(UUID seasonId, Standing.StandingType type);
}
