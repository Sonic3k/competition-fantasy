package com.fantasy.competition.repository;
import com.fantasy.competition.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.UUID;
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, UUID> {
    List<PromptTemplate> findByCategoryOrderByName(String category);
    List<PromptTemplate> findAllByOrderByName();
}
