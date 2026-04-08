package com.ifreelife.carrertry.controller;

import com.ifreelife.carrertry.dto.ApplyRequest;
import com.ifreelife.carrertry.entity.CareerPlan;
import com.ifreelife.carrertry.entity.ErrorCorrectionRecord;
import com.ifreelife.carrertry.entity.JobPosting;
import com.ifreelife.carrertry.entity.ResumeRecord;
import com.ifreelife.carrertry.entity.StudentAchievement;
import com.ifreelife.carrertry.entity.StudentProfile;
import com.ifreelife.carrertry.entity.StudentApplication;
import com.ifreelife.carrertry.service.JobService;
import com.ifreelife.carrertry.service.MilestoneService;
import com.ifreelife.carrertry.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/student")
public class StudentController {

    private final JobService jobService;
    private final StudentService studentService;
    private final MilestoneService milestoneService;

    @GetMapping("/jobs")
    public List<JobPosting> browseJobs() {
        return jobService.listApprovedJobs();
    }

    @GetMapping("/home")
    public Page<JobPosting> home(@RequestParam(defaultValue = "0") int page) {
        return milestoneService.studentHomeJobs(page);
    }

    @GetMapping("/jobs/{id}")
    public JobPosting jobDetail(@PathVariable Long id) {
        return jobService.getApprovedById(id);
    }

    @PostMapping("/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentApplication apply(@RequestBody @Valid ApplyRequest request) {
        return studentService.apply(request);
    }

    @GetMapping("/applications")
    public List<StudentApplication> myApplications() {
        return studentService.listMyApplications();
    }

    @GetMapping("/matches")
    public List<JobPosting> match(@RequestParam(required = false) String keywords) {
        return studentService.matchJobs(keywords);
    }

    @PostMapping("/onboarding")
    public StudentProfile completeOnboarding(@RequestBody Map<String, Object> request) {
        return milestoneService.completeOnboarding(
            String.valueOf(request.getOrDefault("techStack", "")),
            String.valueOf(request.getOrDefault("capabilityInfo", "")),
            String.valueOf(request.getOrDefault("mbtiType", "")),
            Boolean.parseBoolean(String.valueOf(request.getOrDefault("commitmentAgreed", false)))
        );
    }

    @GetMapping("/onboarding/status")
    public Map<String, Object> onboardingStatus() {
        return Map.of("canApplyAndPortrait", milestoneService.canStudentApply());
    }

    @PostMapping("/activity")
    public Map<String, Object> recordActivity(@RequestBody Map<String, Object> request) {
        int activeSeconds = parseIntField(request.get("activeSeconds"), "activeSeconds");
        boolean viewedJobs = parseBooleanField(request.get("viewedJobs"), "viewedJobs");
        boolean refreshedResume = parseBooleanField(request.get("refreshedResume"), "refreshedResume");
        return milestoneService.recordDailyActivity(activeSeconds, viewedJobs, refreshedResume);
    }

    @PostMapping("/check-in")
    public Map<String, Object> checkIn() {
        return milestoneService.checkInToday();
    }

    @GetMapping("/check-in/summary")
    public Map<String, Object> checkInSummary() {
        return milestoneService.myDailyCheckInSummary();
    }

    @GetMapping("/profile")
    public StudentProfile profile() {
        return milestoneService.myProfile();
    }

    @PutMapping("/center")
    public Map<String, Object> updateCenter(@RequestBody Map<String, String> request) {
        return Map.of(
            "user",
            milestoneService.updateStudentBaseInfo(
                request.getOrDefault("displayName", ""),
                request.getOrDefault("phone", ""),
                request.getOrDefault("major", "")
            )
        );
    }

    @PostMapping("/resumes")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeRecord uploadResume(@RequestBody Map<String, String> request) {
        return milestoneService.saveResume(
            request.getOrDefault("fileName", "resume.txt"),
            request.getOrDefault("content", "")
        );
    }

    @GetMapping("/resumes")
    public List<ResumeRecord> resumes() {
        return milestoneService.listMyResumes();
    }

    @GetMapping("/portrait/matches")
    public List<Map<String, Object>> portraitMatches() {
        return milestoneService.jobMatchScores();
    }

    @PostMapping("/plans")
    public CareerPlan savePlan(@RequestBody Map<String, Object> request) {
        int progress = Integer.parseInt(String.valueOf(request.getOrDefault("progressPercent", "0")));
        return milestoneService.saveCareerPlan(
            String.valueOf(request.getOrDefault("targetCity", "")),
            String.valueOf(request.getOrDefault("targetCareer", "")),
            progress,
            String.valueOf(request.getOrDefault("dynamicAdjustment", ""))
        );
    }

    @GetMapping("/plans")
    public CareerPlan myPlan() {
        return milestoneService.myCareerPlan();
    }

    @GetMapping("/plans/pdf")
    public ResponseEntity<byte[]> planPdf() {
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header("Content-Disposition", "attachment; filename=career-plan.pdf")
            .body(milestoneService.downloadCareerPlanPdf());
    }

    @GetMapping("/notices")
    public List<?> notices() {
        return milestoneService.noticesForCurrentUser();
    }

    @GetMapping("/achievements")
    public List<StudentAchievement> achievements() {
        return milestoneService.myAchievements();
    }

    @PostMapping("/corrections")
    public ErrorCorrectionRecord correction(@RequestBody Map<String, String> request) {
        return milestoneService.recordCorrection(
            request.getOrDefault("wrongPoint", ""),
            request.getOrDefault("correctionAction", ""),
            request.getOrDefault("closedLoopStatus", "OPEN")
        );
    }

    @GetMapping("/corrections")
    public List<ErrorCorrectionRecord> corrections() {
        return milestoneService.myCorrectionRecords();
    }

    private int parseIntField(Object value, String fieldName) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be an integer");
        }
    }

    private boolean parseBooleanField(Object value, String fieldName) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String normalized = String.valueOf(value).trim();
        if ("true".equalsIgnoreCase(normalized)) {
            return true;
        }
        if ("false".equalsIgnoreCase(normalized)) {
            return false;
        }
        throw new IllegalArgumentException(fieldName + " must be true or false");
    }
}
