package com.fantasy.competition.repository;

import com.fantasy.competition.entity.StageGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StageGroupRepository extends JpaRepository<StageGroup, UUID> {
    List<StageGroup> findByStageId(UUID stageId);
}
