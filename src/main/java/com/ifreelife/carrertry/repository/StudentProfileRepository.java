package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByStudentUsername(String studentUsername);
}
