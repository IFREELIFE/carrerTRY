package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "student_report")
public class StudentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String studentUsername;

    @Column(nullable = false, length = 32)
    private String reportType;

    @Column(nullable = false, length = 128)
    private String reportTitle;

    @Lob
    @Column(nullable = false)
    private String reportSummary;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
