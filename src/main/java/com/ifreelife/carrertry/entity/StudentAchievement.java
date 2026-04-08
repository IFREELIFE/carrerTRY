package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
    name = "student_achievement",
    indexes = {@Index(name = "idx_student_achievement_student", columnList = "studentUsername")}
)
public class StudentAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String studentUsername;

    @Column(nullable = false, length = 64)
    private String achievementCode;

    @Column(nullable = false)
    private LocalDateTime achievedAt = LocalDateTime.now();
}
