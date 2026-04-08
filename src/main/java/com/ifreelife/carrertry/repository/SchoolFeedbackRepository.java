package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.SchoolFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolFeedbackRepository extends JpaRepository<SchoolFeedback, Long> {
    List<SchoolFeedback> findBySchoolNameAndStudentNameOrderByCreatedAtDesc(String schoolName, String studentName);
}
