package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
    name = "student_application",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_application_student_job", columnNames = {"studentUsername", "jobId"})
    }
)
public class StudentApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long jobId;

    @Column(nullable = false)
    private String studentUsername;

    @Column(nullable = false)
    private String studentName;

    @Column(nullable = false)
    private String resumeSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(length = 1024)
    private String interviewNotice;

    @Column(length = 2048)
    private String interviewFeedback;

    @Column(length = 1024)
    private String teacherCommentSnapshot;

    @Column(nullable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();
}
