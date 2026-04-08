package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.dto.JobCreateRequest;
import com.ifreelife.carrertry.dto.JobImportResult;
import com.ifreelife.carrertry.entity.JobPosting;
import com.ifreelife.carrertry.entity.JobStatus;
import com.ifreelife.carrertry.entity.UserAccount;
import com.ifreelife.carrertry.entity.neo4j.SkillNode;
import com.ifreelife.carrertry.repository.JobPostingRepository;
import com.ifreelife.carrertry.repository.UserAccountRepository;
import com.ifreelife.carrertry.repository.neo4j.SkillNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JobService {
    private static final int EXPECTED_EXCEL_COLUMN_COUNT = 9;
    private static final int MAX_DECIMAL_INPUT_LENGTH = 32;
    private static final String[] EXPECTED_HEADER_COLUMNS = {
        "title",
        "department",
        "location",
        "salarymin",
        "salarymax",
        "experiencerequirement",
        "educationrequirement",
        "skills",
        "description"
    };

    private final JobPostingRepository jobPostingRepository;
    private final SkillNodeRepository skillNodeRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public JobPosting create(JobCreateRequest request) {
        return createInternal(request);
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
                if (lineNo == 1) {
                    if (!isExpectedHeader(line)) {
                        throw new IllegalArgumentException("Missing or invalid header row in excel file");
                    }
                    continue;
                }
                List<String> cells = parseCsvLine(line);
                if (cells.size() < EXPECTED_EXCEL_COLUMN_COUNT) {
                    throw new IllegalArgumentException("Invalid excel row at line " + lineNo);
                }
                JobCreateRequest request = new JobCreateRequest();
                request.setTitle(cells.get(0).trim());
                request.setDepartment(cells.get(1).trim());
                request.setLocation(cells.get(2).trim());
                request.setSalaryMin(parseDecimal(cells.get(3).trim(), lineNo));
                request.setSalaryMax(parseDecimal(cells.get(4).trim(), lineNo));
                request.setExperienceRequirement(cells.get(5).trim());
                request.setEducationRequirement(cells.get(6).trim());
                request.setSkills(cells.get(7).trim());
                request.setDescription(cells.get(8).trim());
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
        String currentEnterpriseName = loadCurrentEnterpriseName();
        if (enterpriseName == null || enterpriseName.isBlank()) {
            return jobPostingRepository.findByEnterpriseNameContainingIgnoreCase(currentEnterpriseName, pageRequest);
        }
        ensureEnterpriseScope(enterpriseName);
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

    public JobPosting getApprovedById(Long id) {
        JobPosting job = getById(id);
        if (job.getStatus() != JobStatus.APPROVED) {
            throw new IllegalArgumentException("Approved job not found: " + id);
        }
        return job;
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
        validateSalaryRange(request);
        ensureEnterpriseScope(request.getEnterpriseName());
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
        List<String> cells = parseCsvLine(line);
        if (cells.size() < EXPECTED_EXCEL_COLUMN_COUNT) {
            return false;
        }
        for (int i = 0; i < EXPECTED_EXCEL_COLUMN_COUNT; i++) {
            if (!cells.get(i).trim().toLowerCase().equals(EXPECTED_HEADER_COLUMNS[i])) {
                return false;
            }
        }
        return true;
    }

    private List<String> parseCsvLine(String line) {
        if (line == null) {
            return Collections.emptyList();
        }
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }
            if (ch == ',' && !inQuotes) {
                cells.add(current.toString());
                current = new StringBuilder();
                continue;
            }
            current.append(ch);
        }
        cells.add(current.toString());
        return cells;
    }

    private void ensureEnterpriseScope(String enterpriseName) {
        String currentEnterpriseName = loadCurrentEnterpriseName();
        if (!currentEnterpriseName.equalsIgnoreCase(enterpriseName.trim())) {
            throw new IllegalArgumentException("enterpriseName does not match current enterprise account");
        }
    }

    private String loadCurrentEnterpriseName() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAccount account = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        if (account.getEnterpriseName() == null || account.getEnterpriseName().isBlank()) {
            throw new IllegalArgumentException("Current account has no enterprise binding");
        }
        return account.getEnterpriseName().trim();
    }

    private BigDecimal parseDecimal(String value, int lineNo) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Salary value is required at line " + lineNo);
        }
        if (value.length() > MAX_DECIMAL_INPUT_LENGTH) {
            throw new IllegalArgumentException("Salary value exceeds maximum length at line " + lineNo);
        }
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
