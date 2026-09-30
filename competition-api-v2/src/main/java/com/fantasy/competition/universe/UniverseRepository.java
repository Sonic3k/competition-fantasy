package com.fantasy.competition.universe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UniverseRepository extends JpaRepository<Universe, Long> {
    Optional<Universe> findByKey(String key);
    List<Universe> findAllByOrderByNameAsc();
}
