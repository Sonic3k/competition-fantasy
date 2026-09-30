package com.fantasy.competition.competition;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface StageGroupTeamRepository extends JpaRepository<StageGroupTeam, Long> {
    List<StageGroupTeam> findByStageGroupIdOrderByPositionAsc(Long stageGroupId);
    List<StageGroupTeam> findByStageGroupIdInOrderByPositionAsc(Collection<Long> stageGroupIds);
}
