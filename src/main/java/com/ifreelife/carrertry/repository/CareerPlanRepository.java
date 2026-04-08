package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.CareerPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CareerPlanRepository extends JpaRepository<CareerPlan, Long> {
    Optional<CareerPlan> findByStudentUsername(String studentUsername);
}
