package com.ifreelife.carrertry.controller;

import com.ifreelife.carrertry.dto.ApplyRequest;
import com.ifreelife.carrertry.entity.JobPosting;
import com.ifreelife.carrertry.entity.StudentApplication;
import com.ifreelife.carrertry.service.JobService;
import com.ifreelife.carrertry.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/student")
public class StudentController {

    private final JobService jobService;
    private final StudentService studentService;

    @GetMapping("/jobs")
    public List<JobPosting> browseJobs() {
        return jobService.listApprovedJobs();
    }

    @GetMapping("/jobs/{id}")
    public JobPosting jobDetail(@PathVariable Long id) {
        return jobService.getById(id);
    }

    @PostMapping("/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentApplication apply(@RequestBody @Valid ApplyRequest request) {
        return studentService.apply(request);
    }

    @GetMapping("/matches")
    public List<JobPosting> match(@RequestParam(required = false) String keywords) {
        return studentService.matchJobs(keywords);
    }
}
