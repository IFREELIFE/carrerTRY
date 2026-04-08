package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.dto.SchoolFeedbackRequest;
import com.ifreelife.carrertry.entity.SchoolFeedback;
import com.ifreelife.carrertry.entity.UserAccount;
import com.ifreelife.carrertry.entity.UserRole;
import com.ifreelife.carrertry.repository.SchoolFeedbackRepository;
import com.ifreelife.carrertry.repository.StudentProfileRepository;
import com.ifreelife.carrertry.repository.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchoolServiceTest {

    @Mock
    private SchoolFeedbackRepository schoolFeedbackRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private StudentProfileRepository studentProfileRepository;

    @InjectMocks
    private SchoolService schoolService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createFeedbackShouldRequireSchoolRole() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("u1", "N/A"));
        UserAccount user = new UserAccount();
        user.setUsername("u1");
        user.setRole(UserRole.STUDENT);
        user.setSchoolName("S1");
        when(userAccountRepository.findByUsername("u1")).thenReturn(Optional.of(user));

        SchoolFeedbackRequest request = new SchoolFeedbackRequest();
        request.setStudentName("张三");
        request.setMentor("导师");
        request.setComment("评语");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> schoolService.createFeedback(request));
        assertEquals("Only school can operate school feedback", ex.getMessage());
    }

    @Test
    void createFeedbackShouldTrimRequestFields() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("school1", "N/A"));
        UserAccount user = new UserAccount();
        user.setUsername("school1");
        user.setRole(UserRole.SCHOOL);
        user.setSchoolName("  某高校  ");
        when(userAccountRepository.findByUsername("school1")).thenReturn(Optional.of(user));
        when(schoolFeedbackRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        SchoolFeedbackRequest request = new SchoolFeedbackRequest();
        request.setStudentName("  张三  ");
        request.setMentor("  李老师 ");
        request.setComment("  很不错  ");

        schoolService.createFeedback(request);

        ArgumentCaptor<SchoolFeedback> captor = ArgumentCaptor.forClass(SchoolFeedback.class);
        verify(schoolFeedbackRepository).save(captor.capture());
        SchoolFeedback saved = captor.getValue();
        assertEquals("张三", saved.getStudentName());
        assertEquals("李老师", saved.getMentor());
        assertEquals("很不错", saved.getComment());
        assertEquals("某高校", saved.getSchoolName());
    }

    @Test
    void queryByStudentShouldRejectBlankName() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("school1", "N/A"));
        UserAccount user = new UserAccount();
        user.setUsername("school1");
        user.setRole(UserRole.SCHOOL);
        user.setSchoolName("某高校");
        when(userAccountRepository.findByUsername("school1")).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> schoolService.queryByStudent("  "));
        assertEquals("studentName cannot be blank", ex.getMessage());
    }
}
