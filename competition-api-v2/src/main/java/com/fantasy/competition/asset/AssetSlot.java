package com.fantasy.competition.asset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/** An asset attached to a slot of an entity, e.g. TEAM 12 / LOGO. One current image per slot, older ones kept. */
@Entity
@Table(name = "asset_slots")
@Getter
@Setter
@NoArgsConstructor
public class AssetSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_type", nullable = false, length = 32)
    private String ownerType;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 32)
    private String slot;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(nullable = false)
    private boolean current = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
