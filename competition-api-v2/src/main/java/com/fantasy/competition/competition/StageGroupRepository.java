package com.fantasy.competition.competition;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StageGroupRepository extends JpaRepository<StageGroup, Long> {
    List<StageGroup> findByStageIdOrderByOrdinalAsc(Long stageId);
    List<StageGroup> findByStageIdInOrderByOrdinalAsc(Collection<Long> stageIds);
    Optional<StageGroup> findByStageIdAndKey(Long stageId, String key);
}
