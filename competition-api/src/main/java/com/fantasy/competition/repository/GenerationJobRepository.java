package com.fantasy.competition.repository;
import com.fantasy.competition.entity.GenerationJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.UUID;
public interface GenerationJobRepository extends JpaRepository<GenerationJob, UUID> {
    List<GenerationJob> findAllByOrderByCreatedAtDesc();
}
