package com.fantasy.competition.competition;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoundRepository extends JpaRepository<Round, Long> {
    List<Round> findByStageGroupIdOrderByNumberAsc(Long stageGroupId);
    List<Round> findByStageGroupIdInOrderByNumberAsc(Collection<Long> stageGroupIds);
    Optional<Round> findByStageGroupIdAndNumber(Long stageGroupId, Integer number);
}
