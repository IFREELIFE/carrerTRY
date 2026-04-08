package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
    name = "career_plan",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_career_plan_student", columnNames = "studentUsername")
    }
)
public class CareerPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String studentUsername;

    @Column(nullable = false, length = 64)
    private String targetCity;

    @Column(nullable = false, length = 64)
    private String targetCareer;

    @Column(nullable = false)
    private Integer matchPercent;

    @Lob
    @Column(nullable = false)
    private String careerPlanning;

    @Lob
    @Column(nullable = false)
    private String trainingPlan;

    @Column(nullable = false)
    private Integer progressPercent = 0;

    @Lob
    private String dynamicAdjustment;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
