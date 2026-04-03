package com.fantasy.competition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "generation_jobs")
@Getter @Setter @NoArgsConstructor
public class GenerationJob extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_template_id")
    private PromptTemplate promptTemplate;
    @Column(nullable = false, columnDefinition = "TEXT") private String resolvedPrompt;
    @Column(nullable = false) private String status = "PENDING";
    private Integer resultCount = 0;
    @Column(columnDefinition = "TEXT") private String errorMessage;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<GenerationResult> results = new ArrayList<>();
}
