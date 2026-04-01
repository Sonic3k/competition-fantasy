package com.fantasy.competition.repository;

import com.fantasy.competition.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StageRepository extends JpaRepository<Stage, UUID> {
    List<Stage> findBySeasonIdOrderByOrderNumber(UUID seasonId);
}
