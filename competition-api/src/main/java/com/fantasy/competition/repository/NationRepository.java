package com.fantasy.competition.repository;

import com.fantasy.competition.entity.Nation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NationRepository extends JpaRepository<Nation, UUID> {
    List<Nation> findByUniverseId(UUID universeId);
}
