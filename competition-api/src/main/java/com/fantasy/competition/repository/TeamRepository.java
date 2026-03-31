package com.fantasy.competition.repository;

import com.fantasy.competition.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    List<Team> findByUniverseId(UUID universeId);
    List<Team> findByNationId(UUID nationId);
}
