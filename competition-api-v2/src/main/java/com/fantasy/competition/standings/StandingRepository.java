package com.fantasy.competition.standings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StandingRepository extends JpaRepository<Standing, Long> {
    List<Standing> findByStageGroupIdIn(Collection<Long> stageGroupIds);
    List<Standing> findByStageGroupIdOrderByTypeAscCheckpointRoundAsc(Long stageGroupId);
    Optional<Standing> findByStageGroupIdAndTypeAndCheckpointRound(Long stageGroupId, StandingType type, Integer checkpointRound);
    @Modifying
    void deleteByStageGroupIdAndType(Long stageGroupId, StandingType type);
}
