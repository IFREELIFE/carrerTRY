package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.JobPosting;
import com.ifreelife.carrertry.entity.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByStatus(JobStatus status);

    Page<JobPosting> findByStatus(JobStatus status, Pageable pageable);

    Page<JobPosting> findByEnterpriseNameContainingIgnoreCase(String enterpriseName, Pageable pageable);

    boolean existsByEnterpriseNameAndTitleAndDepartmentAndLocation(
        String enterpriseName,
        String title,
        String department,
        String location
    );
}
