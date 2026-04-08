package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.StudentApplication;
import com.ifreelife.carrertry.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentApplicationRepository extends JpaRepository<StudentApplication, Long> {
    boolean existsByStudentUsernameAndJobId(String studentUsername, Long jobId);

    List<StudentApplication> findByStudentUsernameOrderByAppliedAtDesc(String studentUsername);

    List<StudentApplication> findByJobIdOrderByAppliedAtDesc(Long jobId);

    List<StudentApplication> findByStatusOrderByAppliedAtDesc(ApplicationStatus status);

    List<StudentApplication> findByTeacherCommentSnapshotContainingIgnoreCaseOrResumeSummaryContainingIgnoreCaseOrderByAppliedAtDesc(
        String teacherCommentKeyword,
        String resumeKeyword
    );
}
