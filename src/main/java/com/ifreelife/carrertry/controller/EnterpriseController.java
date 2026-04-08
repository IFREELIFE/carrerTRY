package com.ifreelife.carrertry.controller;

import com.ifreelife.carrertry.dto.JobCreateRequest;
import com.ifreelife.carrertry.dto.JobImportRequest;
import com.ifreelife.carrertry.dto.JobImportResult;
import com.ifreelife.carrertry.entity.JobPosting;
import com.ifreelife.carrertry.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/enterprise/jobs")
public class EnterpriseController {

    private final JobService jobService;

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
        @RequestParam String enterpriseName
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
}
