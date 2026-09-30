package com.fantasy.competition.format;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FormatPresetRepository extends JpaRepository<FormatPreset, Long> {
    Optional<FormatPreset> findByKey(String key);
    List<FormatPreset> findAllByOrderByFamilyAscNameAsc();
}
