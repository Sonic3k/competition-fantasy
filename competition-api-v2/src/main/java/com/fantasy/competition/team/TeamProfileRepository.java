package com.fantasy.competition.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TeamProfileRepository extends JpaRepository<TeamProfile, Long> {
    List<TeamProfile> findByTeamIdOrderByYearDesc(Long teamId);
    Optional<TeamProfile> findByTeamIdAndYear(Long teamId, Integer year);
    List<TeamProfile> findByTeamIdInAndYear(Collection<Long> teamIds, Integer year);
    List<TeamProfile> findByTeamIdIn(Collection<Long> teamIds);
}
