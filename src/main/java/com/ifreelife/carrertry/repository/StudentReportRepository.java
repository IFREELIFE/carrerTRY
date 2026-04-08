package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.StudentReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentReportRepository extends JpaRepository<StudentReport, Long> {
    List<StudentReport> findByStudentUsernameOrderByCreatedAtDesc(String studentUsername);
}
