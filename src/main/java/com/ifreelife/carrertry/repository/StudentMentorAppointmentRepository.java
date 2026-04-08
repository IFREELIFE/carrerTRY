package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.StudentMentorAppointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentMentorAppointmentRepository extends JpaRepository<StudentMentorAppointment, Long> {
    List<StudentMentorAppointment> findByStudentUsernameOrderByCreatedAtDesc(String studentUsername);
}
