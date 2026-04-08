package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.dto.JobCreateRequest;
import com.ifreelife.carrertry.entity.JobPosting;
import com.ifreelife.carrertry.entity.JobStatus;
import com.ifreelife.carrertry.entity.neo4j.SkillNode;
import com.ifreelife.carrertry.repository.JobPostingRepository;
import com.ifreelife.carrertry.repository.neo4j.SkillNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobPostingRepository jobPostingRepository;
    private final SkillNodeRepository skillNodeRepository;

    @Transactional
    public JobPosting create(JobCreateRequest request) {
        JobPosting jobPosting = new JobPosting();
        jobPosting.setTitle(request.getTitle());
        jobPosting.setDescription(request.getDescription());
        jobPosting.setEnterpriseName(request.getEnterpriseName());
        jobPosting.setSkills(request.getSkills());
        jobPosting.setStatus(JobStatus.PENDING);

        saveSkills(request.getSkills());
        return jobPostingRepository.save(jobPosting);
    }

    @Transactional
    public List<JobPosting> importBatch(List<JobCreateRequest> requests) {
        return requests.stream().map(this::create).toList();
    }

    public List<JobPosting> listApprovedJobs() {
        return jobPostingRepository.findByStatus(JobStatus.APPROVED);
    }

    public List<JobPosting> listAllJobs() {
        return jobPostingRepository.findAll();
    }

    public JobPosting getById(Long id) {
        return jobPostingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
    }

    @Transactional
    public JobPosting approve(Long id) {
        JobPosting job = getById(id);
        job.setStatus(JobStatus.APPROVED);
        return jobPostingRepository.save(job);
    }

    @Transactional
    public JobPosting reject(Long id) {
        JobPosting job = getById(id);
        job.setStatus(JobStatus.REJECTED);
        return jobPostingRepository.save(job);
    }

    private void saveSkills(String rawSkills) {
        if (rawSkills == null || rawSkills.isBlank()) {
            return;
        }
        List<SkillNode> nodes = new ArrayList<>();
        for (String s : rawSkills.split(",")) {
            String skill = s.trim();
            if (!skill.isEmpty()) {
                nodes.add(new SkillNode(skill));
            }
        }
        if (!nodes.isEmpty()) {
            skillNodeRepository.saveAll(nodes);
        }
    }
}
