package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "rag_record")
public class RagRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false)
    private String queryText;

    @Lob
    @Column(nullable = false)
    private String retrievedContext;

    @Column(nullable = false)
    private Double qualityScore;

    @Column(nullable = false)
    private Double confidence;

    @Column(nullable = false)
    private Boolean released;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
