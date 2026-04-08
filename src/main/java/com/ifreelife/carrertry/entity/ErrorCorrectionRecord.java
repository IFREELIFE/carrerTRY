package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "error_correction_record")
public class ErrorCorrectionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String studentUsername;

    @Lob
    @Column(nullable = false)
    private String wrongPoint;

    @Lob
    @Column(nullable = false)
    private String correctionAction;

    @Column(nullable = false, length = 32)
    private String closedLoopStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
