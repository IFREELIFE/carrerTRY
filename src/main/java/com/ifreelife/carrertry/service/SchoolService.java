package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.dto.SchoolFeedbackRequest;
import com.ifreelife.carrertry.entity.SchoolFeedback;
import com.ifreelife.carrertry.entity.UserAccount;
import com.ifreelife.carrertry.repository.SchoolFeedbackRepository;
import com.ifreelife.carrertry.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolFeedbackRepository schoolFeedbackRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public SchoolFeedback createFeedback(SchoolFeedbackRequest request) {
        UserAccount account = loadCurrentSchoolAccount();
        SchoolFeedback feedback = new SchoolFeedback();
        feedback.setStudentName(request.getStudentName());
        feedback.setSchoolName(account.getSchoolName().trim());
        feedback.setMentor(request.getMentor());
        feedback.setComment(request.getComment());
        return schoolFeedbackRepository.save(feedback);
    }

    public List<SchoolFeedback> queryByStudent(String studentName) {
        UserAccount account = loadCurrentSchoolAccount();
        return schoolFeedbackRepository.findBySchoolNameAndStudentNameOrderByCreatedAtDesc(
            account.getSchoolName().trim(),
            studentName
        );
    }

    private UserAccount loadCurrentSchoolAccount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAccount account = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        if (account.getSchoolName() == null || account.getSchoolName().isBlank()) {
            throw new IllegalArgumentException("Current account has no school binding");
        }
        return account;
    }
}
