package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
    name = "student_daily_activity",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_daily_activity_user_date", columnNames = {"studentUsername", "activityDate"})
    }
)
public class StudentDailyActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String studentUsername;

    @Column(nullable = false)
    private LocalDate activityDate;

    @Column(nullable = false)
    private Integer activeSeconds = 0;

    @Column(nullable = false)
    private Integer viewedJobsCount = 0;

    @Column(nullable = false)
    private Boolean resumeRefreshed = Boolean.FALSE;

    @Column(nullable = false)
    private Boolean checkedIn = Boolean.FALSE;

    private LocalDateTime checkedInAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
