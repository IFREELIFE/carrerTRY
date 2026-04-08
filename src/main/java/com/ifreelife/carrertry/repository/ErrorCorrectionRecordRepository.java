package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.ErrorCorrectionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ErrorCorrectionRecordRepository extends JpaRepository<ErrorCorrectionRecord, Long> {
    List<ErrorCorrectionRecord> findByStudentUsernameOrderByCreatedAtDesc(String studentUsername);
}
