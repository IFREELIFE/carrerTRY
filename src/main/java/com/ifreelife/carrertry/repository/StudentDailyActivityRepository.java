package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.StudentDailyActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentDailyActivityRepository extends JpaRepository<StudentDailyActivity, Long> {
    Optional<StudentDailyActivity> findByStudentUsernameAndActivityDate(String studentUsername, LocalDate activityDate);

    List<StudentDailyActivity> findByStudentUsernameAndCheckedInTrueOrderByActivityDateDesc(String studentUsername);
}
