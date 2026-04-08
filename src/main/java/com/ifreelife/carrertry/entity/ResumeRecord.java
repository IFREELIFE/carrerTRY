package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "resume_record")
public class ResumeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String studentUsername;

    @Column(nullable = false, length = 256)
    private String fileName;

    @Lob
    @Column(nullable = false)
    private String rawContent;

    @Lob
    private String aiSuggestion;

    @Lob
    private String beautifiedContent;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
