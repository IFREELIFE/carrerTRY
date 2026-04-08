package com.ifreelife.carrertry.controller;

import com.ifreelife.carrertry.dto.JobCreateRequest;
import com.ifreelife.carrertry.dto.JobImportRequest;
import com.ifreelife.carrertry.dto.JobImportResult;
import com.ifreelife.carrertry.entity.JobPosting;
import com.ifreelife.carrertry.entity.RagRecord;
import com.ifreelife.carrertry.entity.StudentApplication;
import com.ifreelife.carrertry.service.JobService;
import com.ifreelife.carrertry.service.MilestoneService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/enterprise/jobs")
public class EnterpriseController {

    private final JobService jobService;
    private final MilestoneService milestoneService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobPosting create(@RequestBody @Valid JobCreateRequest request) {
        return jobService.create(request);
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    public JobImportResult importBatch(@RequestBody @Valid JobImportRequest request) {
        return jobService.importBatch(request.getJobs());
    }

    @PostMapping("/import/excel")
    @ResponseStatus(HttpStatus.CREATED)
    public JobImportResult importExcel(
        @RequestParam("file") MultipartFile file,
        @RequestParam @NotBlank String enterpriseName
    ) {
        return jobService.importExcel(file, enterpriseName);
    }

    @GetMapping
    public Page<JobPosting> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String enterpriseName
    ) {
        return jobService.listEnterpriseJobs(enterpriseName, page, size);
    }

    @GetMapping("/{id}")
    public JobPosting detail(@PathVariable Long id) {
        return jobService.getById(id);
    }

    @GetMapping("/applications")
    public List<StudentApplication> applications() {
        return milestoneService.enterpriseApplications();
    }

    @GetMapping("/applications/screening")
    public List<StudentApplication> screening(@RequestParam(required = false) String keyword) {
        return milestoneService.intelligentScreening(keyword);
    }

    @PatchMapping("/applications/{id}/notify")
    public StudentApplication notifyInterview(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return milestoneService.interviewNotify(id, request.getOrDefault("notice", ""));
    }

    @PatchMapping("/applications/{id}/feedback")
    public StudentApplication feedbackInterview(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        boolean passed = Boolean.parseBoolean(String.valueOf(request.getOrDefault("passed", false)));
        return milestoneService.interviewFeedback(id, String.valueOf(request.getOrDefault("feedback", "")), passed);
    }

    @PostMapping("/rag")
    public RagRecord rag(@RequestBody Map<String, Object> request) {
        return milestoneService.recordRagResult(
            String.valueOf(request.getOrDefault("query", "")),
            String.valueOf(request.getOrDefault("context", "")),
            Double.parseDouble(String.valueOf(request.getOrDefault("qualityScore", "0"))),
            Double.parseDouble(String.valueOf(request.getOrDefault("confidence", "0")))
        );
    }
}
