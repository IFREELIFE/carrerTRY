package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "quality_metric")
public class QualityMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String metricName;

    @Column(nullable = false)
    private Double metricValue;

    @Column(nullable = false)
    private LocalDateTime snapshotTime = LocalDateTime.now();
}
