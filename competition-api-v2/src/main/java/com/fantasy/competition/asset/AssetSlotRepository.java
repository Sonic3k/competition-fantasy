package com.fantasy.competition.asset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AssetSlotRepository extends JpaRepository<AssetSlot, Long> {
    List<AssetSlot> findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(String ownerType, Long ownerId);
    List<AssetSlot> findByOwnerTypeAndOwnerIdAndSlotAndCurrentTrue(String ownerType, Long ownerId, String slot);
    List<AssetSlot> findByOwnerTypeAndOwnerIdInAndCurrentTrue(String ownerType, Collection<Long> ownerIds);
    List<AssetSlot> findByAssetId(Long assetId);
}
