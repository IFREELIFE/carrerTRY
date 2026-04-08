package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.dto.SchoolFeedbackRequest;
import com.ifreelife.carrertry.entity.SchoolFeedback;
import com.ifreelife.carrertry.entity.StudentProfile;
import com.ifreelife.carrertry.entity.UserAccount;
import com.ifreelife.carrertry.repository.SchoolFeedbackRepository;
import com.ifreelife.carrertry.repository.StudentProfileRepository;
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
    private final StudentProfileRepository studentProfileRepository;

    @Transactional
    public SchoolFeedback createFeedback(SchoolFeedbackRequest request) {
        UserAccount account = loadCurrentSchoolAccount();
        String studentName = requireNonBlank(request.getStudentName(), "studentName");
        String mentor = requireNonBlank(request.getMentor(), "mentor");
        String comment = requireNonBlank(request.getComment(), "comment");
        SchoolFeedback feedback = new SchoolFeedback();
        feedback.setStudentName(studentName);
        feedback.setSchoolName(account.getSchoolName().trim());
        feedback.setMentor(mentor);
        feedback.setComment(comment);
        SchoolFeedback saved = schoolFeedbackRepository.save(feedback);
        userAccountRepository.findByRoleAndSchoolNameAndDisplayNameIgnoreCase(
                com.ifreelife.carrertry.entity.UserRole.STUDENT,
                account.getSchoolName().trim(),
                request.getStudentName().trim()
            )
            .flatMap(student -> studentProfileRepository.findByStudentUsername(student.getUsername()))
            .ifPresent(profile -> appendFeedbackToProfile(profile, feedback));
        return saved;
    }

    public List<SchoolFeedback> queryByStudent(String studentName) {
        UserAccount account = loadCurrentSchoolAccount();
        String normalizedStudentName = requireNonBlank(studentName, "studentName");
        return schoolFeedbackRepository.findBySchoolNameAndStudentNameOrderByCreatedAtDesc(
            account.getSchoolName().trim(),
            normalizedStudentName
        );
    }

    private UserAccount loadCurrentSchoolAccount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAccount account = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("School account not found for current user"));
        if (account.getRole() != com.ifreelife.carrertry.entity.UserRole.SCHOOL) {
            throw new IllegalArgumentException("Only school can operate school feedback");
        }
        if (account.getSchoolName() == null || account.getSchoolName().isBlank()) {
            throw new IllegalArgumentException("Current account has no school binding");
        }
        return account;
    }

    private void appendFeedbackToProfile(StudentProfile profile, SchoolFeedback feedback) {
        String previous = profile.getAiSummary() == null ? "" : profile.getAiSummary();
        String merged = (previous + " | 导师评语：" + feedback.getComment()).trim();
        int maxCodePoints = 1800;
        if (merged.codePointCount(0, merged.length()) > maxCodePoints) {
            int start = merged.offsetByCodePoints(0, merged.codePointCount(0, merged.length()) - maxCodePoints);
            merged = merged.substring(start);
        }
        profile.setAiSummary(merged);
        String tags = profile.getPortraitTags() == null ? "" : profile.getPortraitTags();
        if (!tags.contains("导师反馈")) {
            profile.setPortraitTags((tags + ",导师反馈").replaceAll("^,", ""));
        }
        studentProfileRepository.save(profile);
    }

    private String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }
}
