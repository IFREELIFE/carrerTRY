package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.ResumeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRecordRepository extends JpaRepository<ResumeRecord, Long> {
    List<ResumeRecord> findByStudentUsernameOrderByUpdatedAtDesc(String studentUsername);

    long countByStudentUsername(String studentUsername);
}
