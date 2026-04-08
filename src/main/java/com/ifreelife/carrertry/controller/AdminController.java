package com.ifreelife.carrertry.controller;

import com.ifreelife.carrertry.entity.AiTask;
import com.ifreelife.carrertry.entity.AcceptanceChecklistItem;
import com.ifreelife.carrertry.entity.JobPosting;
import com.ifreelife.carrertry.entity.QualityMetric;
import com.ifreelife.carrertry.entity.SystemNotice;
import com.ifreelife.carrertry.service.AdminService;
import com.ifreelife.carrertry.service.JobService;
import com.ifreelife.carrertry.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final JobService jobService;
    private final AdminService adminService;
    private final MilestoneService milestoneService;

    @PatchMapping("/jobs/{id}/approve")
    public JobPosting approve(@PathVariable Long id) {
        return jobService.approve(id);
    }

    @PatchMapping("/jobs/{id}/reject")
    public JobPosting reject(@PathVariable Long id) {
        return jobService.reject(id);
    }

    @GetMapping("/ai-tasks")
    public List<AiTask> aiTasks(@RequestParam(required = false) String status) {
        if (status == null || status.isBlank()) {
            return adminService.listAiTasks();
        }
        return milestoneService.listTasksByStatus(status);
    }

    @PatchMapping("/ai-tasks/{id}/retry")
    public AiTask retryTask(@PathVariable Long id) {
        return milestoneService.retryFailedTask(id);
    }

    @GetMapping("/jobs")
    public List<JobPosting> jobs() {
        return jobService.listAllJobs();
    }

    @PostMapping("/notices")
    public SystemNotice publishNotice(@RequestBody Map<String, String> request) {
        return milestoneService.publishNotice(
            request.getOrDefault("title", ""),
            request.getOrDefault("content", ""),
            request.getOrDefault("audienceRole", "STUDENT")
        );
    }

    @PostMapping("/achievements/init")
    public Map<String, Object> initAchievements() {
        milestoneService.ensureAchievementDefinitions();
        return Map.of("message", "Initialized 30 achievement definitions");
    }

    @GetMapping("/quality-metrics")
    public List<QualityMetric> qualityMetrics() {
        return milestoneService.latestQualityMetrics();
    }

    @PostMapping("/acceptance")
    public AcceptanceChecklistItem updateAcceptance(@RequestBody Map<String, Object> request) {
        Integer stepNo = Integer.parseInt(String.valueOf(request.getOrDefault("stepNo", "0")));
        boolean doneFlag = Boolean.parseBoolean(String.valueOf(request.getOrDefault("doneFlag", "false")));
        return milestoneService.updateAcceptance(
            stepNo,
            String.valueOf(request.getOrDefault("itemName", "")),
            doneFlag,
            String.valueOf(request.getOrDefault("note", ""))
        );
    }

    @GetMapping("/acceptance")
    public List<AcceptanceChecklistItem> acceptance(@RequestParam(required = false) Integer stepNo) {
        return milestoneService.listAcceptance(stepNo);
    }
}
