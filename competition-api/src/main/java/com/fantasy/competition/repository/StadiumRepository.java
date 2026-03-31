package com.fantasy.competition.repository;

import com.fantasy.competition.entity.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StadiumRepository extends JpaRepository<Stadium, UUID> {
    List<Stadium> findByUniverseId(UUID universeId);
}
