package com.fantasy.competition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "generation_results")
@Getter @Setter @NoArgsConstructor
public class GenerationResult extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private GenerationJob job;
    @Column(nullable = false, columnDefinition = "TEXT") private String imageUrl;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_file_id")
    private MediaFile mediaFile;
    private Boolean saved = false;
}
