package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.dto.SchoolFeedbackRequest;
import com.ifreelife.carrertry.entity.SchoolFeedback;
import com.ifreelife.carrertry.repository.SchoolFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolFeedbackRepository schoolFeedbackRepository;

    @Transactional
    public SchoolFeedback createFeedback(SchoolFeedbackRequest request) {
        SchoolFeedback feedback = new SchoolFeedback();
        feedback.setStudentName(request.getStudentName());
        feedback.setMentor(request.getMentor());
        feedback.setComment(request.getComment());
        return schoolFeedbackRepository.save(feedback);
    }

    public List<SchoolFeedback> queryByStudent(String studentName) {
        return schoolFeedbackRepository.findByStudentName(studentName);
    }
}
