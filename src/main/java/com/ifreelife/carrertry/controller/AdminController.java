package com.ifreelife.carrertry.controller;

import com.ifreelife.carrertry.entity.AiTask;
import com.ifreelife.carrertry.entity.JobPosting;
import com.ifreelife.carrertry.service.AdminService;
import com.ifreelife.carrertry.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final JobService jobService;
    private final AdminService adminService;

    @PatchMapping("/jobs/{id}/approve")
    public JobPosting approve(@PathVariable Long id) {
        return jobService.approve(id);
    }

    @PatchMapping("/jobs/{id}/reject")
    public JobPosting reject(@PathVariable Long id) {
        return jobService.reject(id);
    }

    @GetMapping("/ai-tasks")
    public List<AiTask> aiTasks() {
        return adminService.listAiTasks();
    }

    @GetMapping("/jobs")
    public List<JobPosting> jobs() {
        return jobService.listAllJobs();
    }
}
