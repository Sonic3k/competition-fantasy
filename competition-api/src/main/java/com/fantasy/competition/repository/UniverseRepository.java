package com.fantasy.competition.repository;

import com.fantasy.competition.entity.Universe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UniverseRepository extends JpaRepository<Universe, UUID> {}
