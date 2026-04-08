package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.TeacherMentor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherMentorRepository extends JpaRepository<TeacherMentor, Long> {
    List<TeacherMentor> findBySchoolNameOrderByIdDesc(String schoolName);

    Optional<TeacherMentor> findByIdAndSchoolName(Long id, String schoolName);
}
