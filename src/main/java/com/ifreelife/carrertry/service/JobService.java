package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.dto.JobCreateRequest;
import com.ifreelife.carrertry.dto.JobImportResult;
import com.ifreelife.carrertry.entity.JobPosting;
import com.ifreelife.carrertry.entity.JobStatus;
import com.ifreelife.carrertry.entity.neo4j.SkillNode;
import com.ifreelife.carrertry.repository.JobPostingRepository;
import com.ifreelife.carrertry.repository.neo4j.SkillNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JobService {
    private static final int EXPECTED_EXCEL_COLUMN_COUNT = 9;
    private static final String EXPECTED_HEADER =
        "title,department,location,salarymin,salarymax,experiencerequirement,educationrequirement,skills,description";

    private final JobPostingRepository jobPostingRepository;
    private final SkillNodeRepository skillNodeRepository;

    @Transactional
    public JobPosting create(JobCreateRequest request) {
        validateSalaryRange(request);
        if (isDuplicate(
            request.getEnterpriseName(),
            request.getTitle(),
            request.getDepartment(),
            request.getLocation()
        )) {
            throw new IllegalArgumentException("Duplicate job for same enterprise/title/department/location");
        }
        JobPosting jobPosting = new JobPosting();
        jobPosting.setTitle(request.getTitle());
        jobPosting.setDescription(request.getDescription());
        jobPosting.setEnterpriseName(request.getEnterpriseName());
        jobPosting.setDepartment(request.getDepartment());
        jobPosting.setLocation(request.getLocation());
        jobPosting.setSalaryMin(request.getSalaryMin());
        jobPosting.setSalaryMax(request.getSalaryMax());
        jobPosting.setExperienceRequirement(request.getExperienceRequirement());
        jobPosting.setEducationRequirement(request.getEducationRequirement());
        jobPosting.setSkills(request.getSkills());
        jobPosting.setStatus(JobStatus.PENDING);

        saveSkills(request.getSkills());
        return jobPostingRepository.save(jobPosting);
    }

    @Transactional
    public JobImportResult importBatch(List<JobCreateRequest> requests) {
        Set<String> requestKeys = new HashSet<>();
        List<JobPosting> importedJobs = new ArrayList<>();
        int skippedDuplicateCount = 0;
        for (JobCreateRequest request : requests) {
            validateSalaryRange(request);
            String key = dedupKey(
                request.getEnterpriseName(),
                request.getTitle(),
                request.getDepartment(),
                request.getLocation()
            );
            if (requestKeys.contains(key) || isDuplicate(
                request.getEnterpriseName(),
                request.getTitle(),
                request.getDepartment(),
                request.getLocation()
            )) {
                skippedDuplicateCount++;
                continue;
            }
            requestKeys.add(key);
            importedJobs.add(createInternal(request));
        }
        return JobImportResult.builder()
            .importedCount(importedJobs.size())
            .skippedDuplicateCount(skippedDuplicateCount)
            .importedJobs(importedJobs)
            .build();
    }

    @Transactional
    public JobImportResult importExcel(MultipartFile file, String enterpriseName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Excel file is empty");
        }
        if (enterpriseName == null || enterpriseName.isBlank()) {
            throw new IllegalArgumentException("enterpriseName is required");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<JobCreateRequest> requests = new ArrayList<>();
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) {
                    continue;
                }
                if (lineNo == 1 && isExpectedHeader(line)) {
                    continue;
                }
                String[] cells = line.split(",", -1);
                if (cells.length < EXPECTED_EXCEL_COLUMN_COUNT) {
                    throw new IllegalArgumentException("Invalid excel row at line " + lineNo);
                }
                JobCreateRequest request = new JobCreateRequest();
                request.setTitle(cells[0].trim());
                request.setDepartment(cells[1].trim());
                request.setLocation(cells[2].trim());
                request.setSalaryMin(parseDecimal(cells[3].trim(), lineNo));
                request.setSalaryMax(parseDecimal(cells[4].trim(), lineNo));
                request.setExperienceRequirement(cells[5].trim());
                request.setEducationRequirement(cells[6].trim());
                request.setSkills(cells[7].trim());
                request.setDescription(cells[8].trim());
                request.setEnterpriseName(enterpriseName.trim());
                requests.add(request);
            }
            return importBatch(requests);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to parse excel file");
        }
    }

    public Page<JobPosting> listEnterpriseJobs(String enterpriseName, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        if (enterpriseName == null || enterpriseName.isBlank()) {
            return jobPostingRepository.findAll(pageRequest);
        }
        return jobPostingRepository.findByEnterpriseNameContainingIgnoreCase(enterpriseName, pageRequest);
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

    private JobPosting createInternal(JobCreateRequest request) {
        JobPosting jobPosting = new JobPosting();
        jobPosting.setTitle(request.getTitle());
        jobPosting.setDescription(request.getDescription());
        jobPosting.setEnterpriseName(request.getEnterpriseName());
        jobPosting.setDepartment(request.getDepartment());
        jobPosting.setLocation(request.getLocation());
        jobPosting.setSalaryMin(request.getSalaryMin());
        jobPosting.setSalaryMax(request.getSalaryMax());
        jobPosting.setExperienceRequirement(request.getExperienceRequirement());
        jobPosting.setEducationRequirement(request.getEducationRequirement());
        jobPosting.setSkills(request.getSkills());
        jobPosting.setStatus(JobStatus.PENDING);
        saveSkills(request.getSkills());
        return jobPostingRepository.save(jobPosting);
    }

    private boolean isDuplicate(String enterpriseName, String title, String department, String location) {
        return jobPostingRepository.existsByEnterpriseNameAndTitleAndDepartmentAndLocation(
            enterpriseName.trim(),
            title.trim(),
            department.trim(),
            location.trim()
        );
    }

    private String dedupKey(String enterpriseName, String title, String department, String location) {
        return String.join(
            "|",
            enterpriseName.trim().toLowerCase(),
            title.trim().toLowerCase(),
            department.trim().toLowerCase(),
            location.trim().toLowerCase()
        );
    }

    private void validateSalaryRange(JobCreateRequest request) {
        if (request.getSalaryMin() == null || request.getSalaryMax() == null) {
            throw new IllegalArgumentException("salaryMin and salaryMax are required");
        }
        if (request.getSalaryMin().compareTo(request.getSalaryMax()) > 0) {
            throw new IllegalArgumentException("salaryMin cannot be greater than salaryMax");
        }
    }

    private boolean isExpectedHeader(String line) {
        return line.trim().toLowerCase().replace(" ", "").equals(EXPECTED_HEADER);
    }

    private BigDecimal parseDecimal(String value, int lineNo) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid salary value at line " + lineNo);
        }
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
