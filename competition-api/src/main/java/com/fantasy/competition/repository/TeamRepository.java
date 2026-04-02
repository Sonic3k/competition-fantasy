package com.fantasy.competition.repository;

import com.fantasy.competition.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.nation WHERE t.universe.id = :universeId")
    List<Team> findByUniverseId(UUID universeId);

    List<Team> findByNationId(UUID nationId);
}
