package com.fantasy.competition.repository;

import com.fantasy.competition.entity.Jersey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface JerseyRepository extends JpaRepository<Jersey, UUID> {
    List<Jersey> findByTeamId(UUID teamId);
}
