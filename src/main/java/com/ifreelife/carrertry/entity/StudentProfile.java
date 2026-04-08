package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
    name = "student_profile",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_profile_username", columnNames = "studentUsername")
    }
)
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String studentUsername;

    @Column(nullable = false, length = 128)
    private String studentName;

    @Column(length = 512)
    private String techStack;

    @Column(length = 1024)
    private String capabilityInfo;

    @Column(length = 8)
    private String mbtiType;

    @Column(nullable = false)
    private Boolean commitmentAgreed = Boolean.FALSE;

    @Column(nullable = false)
    private Boolean onboardingCompleted = Boolean.FALSE;

    @Column(length = 2048)
    private String portraitVector12;

    @Column(length = 1024)
    private String portraitTags;

    @Column(length = 2048)
    private String aiSummary;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
