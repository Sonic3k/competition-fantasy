package com.fantasy.competition.repository;

import com.fantasy.competition.entity.Standing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StandingRepository extends JpaRepository<Standing, UUID> {
    List<Standing> findBySeasonIdOrderByPointsDescGoalsForDesc(UUID seasonId);
}
