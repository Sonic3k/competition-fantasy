package com.fantasy.competition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prompt_templates")
@Getter @Setter @NoArgsConstructor
public class PromptTemplate extends BaseEntity {
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String category;
    @Column(nullable = false, columnDefinition = "TEXT") private String templateText;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_collection_id")
    private Collection defaultCollection;
}
