package com.fantasy.competition.competition;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TieRepository extends JpaRepository<Tie, Long> {
    List<Tie> findByKoRoundIdOrderByPositionAsc(Long koRoundId);
    List<Tie> findByKoRoundIdInOrderByPositionAsc(Collection<Long> koRoundIds);
}
