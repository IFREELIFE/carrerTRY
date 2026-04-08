package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.TeacherMentor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherMentorRepository extends JpaRepository<TeacherMentor, Long> {
    List<TeacherMentor> findBySchoolNameOrderByIdDesc(String schoolName);
}
