package com.fantasy.competition.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByUniverseIdOrderByNameAsc(Long universeId);
    List<Team> findByUniverseIdAndTypeOrderByNameAsc(Long universeId, TeamType type);
    List<Team> findByNationIdOrderByNameAsc(Long nationId);
    Optional<Team> findByUniverseIdAndKey(Long universeId, String key);
    List<Team> findByUniverseIdAndNameIgnoreCase(Long universeId, String name);
    List<Team> findByIdIn(Collection<Long> ids);
    long countByUniverseId(Long universeId);
}
