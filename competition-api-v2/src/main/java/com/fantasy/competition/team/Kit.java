package com.fantasy.competition.team;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "kits")
@Getter
@Setter
@NoArgsConstructor
public class Kit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(nullable = false)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private KitKind kind = KitKind.HOME;

    @Column(name = "shirt_color", length = 32)
    private String shirtColor;

    @Column(name = "shorts_color", length = 32)
    private String shortsColor;

    @Column(name = "socks_color", length = 32)
    private String socksColor;

    @Column(length = 120)
    private String sponsor;

    @Column(name = "image_asset_id")
    private Long imageAssetId;
}
