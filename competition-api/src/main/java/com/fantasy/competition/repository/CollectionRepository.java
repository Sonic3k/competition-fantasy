package com.fantasy.competition.repository;

import com.fantasy.competition.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CollectionRepository extends JpaRepository<Collection, UUID> {
    List<Collection> findByParentIdOrderByName(UUID parentId);
    List<Collection> findByParentIsNullOrderByName();
    List<Collection> findByUniverseIdOrderByName(UUID universeId);
    List<Collection> findByTypeOrderByName(Collection.CollectionType type);
}
