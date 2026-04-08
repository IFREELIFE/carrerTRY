package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.StudentAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentAchievementRepository extends JpaRepository<StudentAchievement, Long> {
    List<StudentAchievement> findByStudentUsernameOrderByAchievedAtDesc(String studentUsername);

    boolean existsByStudentUsernameAndAchievementCode(String studentUsername, String achievementCode);
}
