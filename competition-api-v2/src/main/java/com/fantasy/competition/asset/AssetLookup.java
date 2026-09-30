package com.fantasy.competition.asset;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Read-side helper: current image URL per owner for a slot, in one query pair. */
@Component
public class AssetLookup {

    private final AssetSlotRepository slots;
    private final AssetRepository assets;

    public AssetLookup(AssetSlotRepository slots, AssetRepository assets) {
        this.slots = slots;
        this.assets = assets;
    }

    public Map<Long, String> currentUrls(String ownerType, Collection<Long> ownerIds, String slot) {
        Map<Long, String> out = new HashMap<>();
        if (ownerIds == null || ownerIds.isEmpty()) return out;
        var current = slots.findByOwnerTypeAndOwnerIdInAndCurrentTrue(ownerType, ownerIds).stream()
                .filter(s -> slot == null || slot.equalsIgnoreCase(s.getSlot()))
                .toList();
        if (current.isEmpty()) return out;
        Map<Long, String> urlById = new HashMap<>();
        for (Asset a : assets.findByIdIn(current.stream().map(AssetSlot::getAssetId).toList())) urlById.put(a.getId(), a.getUrl());
        for (AssetSlot s : current) {
            String url = urlById.get(s.getAssetId());
            if (url != null) out.putIfAbsent(s.getOwnerId(), url);
        }
        return out;
    }
}
