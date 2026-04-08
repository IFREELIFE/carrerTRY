package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "teacher_mentor")
public class TeacherMentor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String schoolName;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 128)
    private String expertise;

    @Column(nullable = false, length = 32)
    private String phone;

    @Column(nullable = false, length = 128)
    private String availableTime;

    @Column(nullable = false, length = 128)
    private String location;
}
