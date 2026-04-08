package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
    name = "student_mentor_appointment",
    indexes = {
        @Index(name = "idx_student_mentor_appt_student", columnList = "studentUsername"),
        @Index(name = "idx_student_mentor_appt_school", columnList = "schoolName")
    }
)
public class StudentMentorAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String studentUsername;

    @Column(nullable = false, length = 128)
    private String studentName;

    @Column(nullable = false, length = 128)
    private String schoolName;

    @Column(nullable = false)
    private Long mentorId;

    @Column(nullable = false, length = 64)
    private String mentorName;

    @Column(nullable = false, length = 128)
    private String appointmentTime;

    @Column(nullable = false, length = 32)
    private String status = "BOOKED";

    @Column(length = 255)
    private String note;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
