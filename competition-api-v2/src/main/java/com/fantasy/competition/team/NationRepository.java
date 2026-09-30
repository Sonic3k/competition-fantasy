package com.fantasy.competition.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NationRepository extends JpaRepository<Nation, Long> {
    List<Nation> findByUniverseIdOrderByNameAsc(Long universeId);
    Optional<Nation> findByUniverseIdAndKey(Long universeId, String key);
    Optional<Nation> findByUniverseIdAndCodeIgnoreCase(Long universeId, String code);
}
