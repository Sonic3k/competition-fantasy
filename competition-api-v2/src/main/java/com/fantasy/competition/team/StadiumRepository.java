package com.fantasy.competition.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StadiumRepository extends JpaRepository<Stadium, Long> {
    List<Stadium> findByUniverseIdOrderByNameAsc(Long universeId);
    Optional<Stadium> findByUniverseIdAndKey(Long universeId, String key);
}
