package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.dto.ApplyRequest;
import com.ifreelife.carrertry.entity.JobPosting;
import com.ifreelife.carrertry.entity.JobStatus;
import com.ifreelife.carrertry.entity.StudentApplication;
import com.ifreelife.carrertry.repository.StudentApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentApplicationRepository studentApplicationRepository;
    private final JobService jobService;

    @Transactional
    public StudentApplication apply(ApplyRequest request) {
        JobPosting jobPosting = jobService.getById(request.getJobId());
        if (jobPosting.getStatus() != JobStatus.APPROVED) {
            throw new IllegalStateException("Job is not open for application");
        }
        StudentApplication application = new StudentApplication();
        application.setJobId(request.getJobId());
        application.setStudentName(request.getStudentName());
        application.setResumeSummary(request.getResumeSummary());
        return studentApplicationRepository.save(application);
    }

    public List<JobPosting> matchJobs(String keywords) {
        if (keywords == null || keywords.isBlank()) {
            return jobService.listApprovedJobs();
        }
        String key = keywords.toLowerCase();
        return jobService.listApprovedJobs().stream()
            .filter(j -> j.getTitle().toLowerCase().contains(key)
                || j.getDescription().toLowerCase().contains(key)
                || j.getSkills().toLowerCase().contains(key))
            .toList();
    }
}
