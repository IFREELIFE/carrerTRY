package com.ifreelife.carrertry.controller;

import com.ifreelife.carrertry.dto.SchoolFeedbackRequest;
import com.ifreelife.carrertry.entity.SchoolFeedback;
import com.ifreelife.carrertry.service.SchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/school")
public class SchoolController {

    private final SchoolService schoolService;

    @PostMapping("/feedbacks")
    @ResponseStatus(HttpStatus.CREATED)
    public SchoolFeedback feedback(@RequestBody @Valid SchoolFeedbackRequest request) {
        return schoolService.createFeedback(request);
    }

    @GetMapping("/feedbacks")
    public List<SchoolFeedback> feedbacks(@RequestParam String studentName) {
        return schoolService.queryByStudent(studentName);
    }
}
