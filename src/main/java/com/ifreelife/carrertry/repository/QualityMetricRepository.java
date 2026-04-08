package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.QualityMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QualityMetricRepository extends JpaRepository<QualityMetric, Long> {
    List<QualityMetric> findTop20ByOrderBySnapshotTimeDesc();
}
