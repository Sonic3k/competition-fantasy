package com.fantasy.competition.repository;

import com.fantasy.competition.entity.MatchSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MatchSlotRepository extends JpaRepository<MatchSlot, UUID> {
    List<MatchSlot> findByMatchId(UUID matchId);
    List<MatchSlot> findBySourceMatchId(UUID sourceMatchId);
}
