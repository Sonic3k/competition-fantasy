package com.fantasy.competition.competition;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface KoRoundRepository extends JpaRepository<KoRound, Long> {
    List<KoRound> findByStageIdOrderByOrdinalAsc(Long stageId);
    List<KoRound> findByStageIdInOrderByOrdinalAsc(Collection<Long> stageIds);
}
