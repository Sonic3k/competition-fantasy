package com.fantasy.competition.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface KitRepository extends JpaRepository<Kit, Long> {
    List<Kit> findByTeamIdOrderByYearDescKindAsc(Long teamId);
    Optional<Kit> findByTeamIdAndYearAndKind(Long teamId, Integer year, KitKind kind);
    List<Kit> findByTeamIdInAndYear(Collection<Long> teamIds, Integer year);
}
