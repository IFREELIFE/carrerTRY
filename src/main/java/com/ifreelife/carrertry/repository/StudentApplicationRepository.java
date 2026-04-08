package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.StudentApplication;
import com.ifreelife.carrertry.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentApplicationRepository extends JpaRepository<StudentApplication, Long> {
    boolean existsByStudentUsernameAndJobId(String studentUsername, Long jobId);

    List<StudentApplication> findByStudentUsernameOrderByAppliedAtDesc(String studentUsername);

    List<StudentApplication> findByJobIdOrderByAppliedAtDesc(Long jobId);

    List<StudentApplication> findByStatusOrderByAppliedAtDesc(ApplicationStatus status);

    List<StudentApplication> findByJobIdInOrderByAppliedAtDesc(List<Long> jobIds);

    @Query("""
        select sa from StudentApplication sa
        where sa.jobId in :jobIds
          and (
            lower(coalesce(sa.teacherCommentSnapshot, '')) like lower(concat('%', :keyword, '%'))
            or lower(coalesce(sa.resumeSummary, '')) like lower(concat('%', :keyword, '%'))
          )
        order by sa.appliedAt desc
        """)
    List<StudentApplication> searchByJobIdsAndKeyword(
        @Param("jobIds") List<Long> jobIds,
        @Param("keyword") String keyword
    );

}
