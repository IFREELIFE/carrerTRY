package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.dto.ApplyRequest;
import com.ifreelife.carrertry.entity.JobPosting;
import com.ifreelife.carrertry.entity.StudentApplication;
import com.ifreelife.carrertry.entity.UserAccount;
import com.ifreelife.carrertry.repository.StudentApplicationRepository;
import com.ifreelife.carrertry.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentApplicationRepository studentApplicationRepository;
    private final JobService jobService;
    private final UserAccountRepository userAccountRepository;
    private final MilestoneService milestoneService;

    @Transactional
    public StudentApplication apply(ApplyRequest request) {
        JobPosting jobPosting = jobService.getApprovedById(request.getJobId());
        UserAccount account = loadCurrentStudentAccount();
        if (!Boolean.TRUE.equals(account.getOnboardingCompleted())) {
            throw new IllegalArgumentException("Complete first-login onboarding before applying");
        }
        if (studentApplicationRepository.existsByStudentUsernameAndJobId(account.getUsername(), request.getJobId())) {
            throw new IllegalArgumentException("Already applied to this job");
        }
        StudentApplication application = new StudentApplication();
        application.setJobId(jobPosting.getId());
        application.setStudentUsername(account.getUsername());
        application.setStudentName(account.getDisplayName());
        application.setResumeSummary(request.getResumeSummary());
        StudentApplication saved = studentApplicationRepository.save(application);
        milestoneService.refreshTeacherCommentSnapshot(saved);
        return saved;
    }

    public List<StudentApplication> listMyApplications() {
        UserAccount account = loadCurrentStudentAccount();
        return studentApplicationRepository.findByStudentUsernameOrderByAppliedAtDesc(account.getUsername());
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

    private UserAccount loadCurrentStudentAccount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Student account not found for current user"));
    }
}
