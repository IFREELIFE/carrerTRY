package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(
    name = "job_posting",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_job_enterprise_title_department_location",
            columnNames = {"enterpriseName", "title", "department", "location"}
        )
    }
)
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String enterpriseName;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salaryMax;

    @Column(nullable = false)
    private String experienceRequirement;

    @Column(nullable = false)
    private String educationRequirement;

    @Column(nullable = false)
    private String skills;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
