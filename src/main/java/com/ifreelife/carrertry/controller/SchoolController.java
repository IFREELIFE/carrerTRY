package com.ifreelife.carrertry.controller;

import com.ifreelife.carrertry.dto.SchoolFeedbackRequest;
import com.ifreelife.carrertry.entity.SchoolFeedback;
import com.ifreelife.carrertry.entity.TeacherMentor;
import com.ifreelife.carrertry.service.MilestoneService;
import com.ifreelife.carrertry.service.SchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/school")
public class SchoolController {

    private final SchoolService schoolService;
    private final MilestoneService milestoneService;

    @PostMapping("/feedbacks")
    @ResponseStatus(HttpStatus.CREATED)
    public SchoolFeedback feedback(@RequestBody @Valid SchoolFeedbackRequest request) {
        return schoolService.createFeedback(request);
    }

    @GetMapping("/feedbacks")
    public List<SchoolFeedback> feedbacks(@RequestParam String studentName) {
        return schoolService.queryByStudent(studentName);
    }

    @PostMapping("/mentors")
    @ResponseStatus(HttpStatus.CREATED)
    public TeacherMentor addMentor(@RequestBody Map<String, String> request) {
        return milestoneService.createMentor(
            request.getOrDefault("name", ""),
            request.getOrDefault("expertise", ""),
            request.getOrDefault("phone", ""),
            request.getOrDefault("availableTime", ""),
            request.getOrDefault("location", "")
        );
    }

    @GetMapping("/mentors")
    public List<TeacherMentor> mentors() {
        return milestoneService.listMentors();
    }

    @GetMapping("/students")
    public Map<String, Object> students(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return milestoneService.schoolStudentsWithProfile(page, size);
    }

    @GetMapping("/students/{username}")
    public Map<String, Object> studentDetail(@PathVariable String username) {
        return milestoneService.schoolStudentDetail(username);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return milestoneService.schoolDashboard();
    }
}
