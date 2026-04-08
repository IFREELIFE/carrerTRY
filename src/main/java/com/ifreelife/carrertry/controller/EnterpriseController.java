package com.ifreelife.carrertry.controller;

import com.ifreelife.carrertry.dto.JobCreateRequest;
import com.ifreelife.carrertry.dto.JobImportRequest;
import com.ifreelife.carrertry.entity.JobPosting;
import com.ifreelife.carrertry.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<JobPosting> importBatch(@RequestBody @Valid JobImportRequest request) {
        return jobService.importBatch(request.getJobs());
    }

    @GetMapping("/{id}")
    public JobPosting detail(@PathVariable Long id) {
        return jobService.getById(id);
    }
}
