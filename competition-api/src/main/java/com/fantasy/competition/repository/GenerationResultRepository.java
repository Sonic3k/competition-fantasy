package com.fantasy.competition.repository;
import com.fantasy.competition.entity.GenerationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.UUID;
public interface GenerationResultRepository extends JpaRepository<GenerationResult, UUID> {
    List<GenerationResult> findByJobIdOrderByCreatedAt(UUID jobId);
}
