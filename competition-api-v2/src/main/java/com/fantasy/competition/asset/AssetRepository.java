package com.fantasy.competition.asset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Page<Asset> findByCollectionOrderByCreatedAtDesc(String collection, Pageable pageable);
    Page<Asset> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Asset> findByIdIn(Collection<Long> ids);
}
