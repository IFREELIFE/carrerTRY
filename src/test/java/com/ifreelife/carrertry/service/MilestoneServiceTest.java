package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.entity.ApplicationStatus;
import com.ifreelife.carrertry.entity.AcceptanceChecklistItem;
import com.ifreelife.carrertry.entity.StudentApplication;
import com.ifreelife.carrertry.entity.UserAccount;
import com.ifreelife.carrertry.entity.UserRole;
import com.ifreelife.carrertry.repository.AcceptanceChecklistItemRepository;
import com.ifreelife.carrertry.repository.AchievementDefinitionRepository;
import com.ifreelife.carrertry.repository.AiTaskRepository;
import com.ifreelife.carrertry.repository.CareerPlanRepository;
import com.ifreelife.carrertry.repository.ErrorCorrectionRecordRepository;
import com.ifreelife.carrertry.repository.JobPostingRepository;
import com.ifreelife.carrertry.repository.QualityMetricRepository;
import com.ifreelife.carrertry.repository.RagRecordRepository;
import com.ifreelife.carrertry.repository.ResumeRecordRepository;
import com.ifreelife.carrertry.repository.SchoolFeedbackRepository;
import com.ifreelife.carrertry.repository.StudentAchievementRepository;
import com.ifreelife.carrertry.repository.StudentApplicationRepository;
import com.ifreelife.carrertry.repository.StudentProfileRepository;
import com.ifreelife.carrertry.repository.SystemNoticeRepository;
import com.ifreelife.carrertry.repository.TeacherMentorRepository;
import com.ifreelife.carrertry.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MilestoneServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private StudentProfileRepository studentProfileRepository;
    @Mock
    private ResumeRecordRepository resumeRecordRepository;
    @Mock
    private JobPostingRepository jobPostingRepository;
    @Mock
    private StudentApplicationRepository studentApplicationRepository;
    @Mock
    private SchoolFeedbackRepository schoolFeedbackRepository;
    @Mock
    private TeacherMentorRepository teacherMentorRepository;
    @Mock
    private CareerPlanRepository careerPlanRepository;
    @Mock
    private SystemNoticeRepository systemNoticeRepository;
    @Mock
    private AchievementDefinitionRepository achievementDefinitionRepository;
    @Mock
    private StudentAchievementRepository studentAchievementRepository;
    @Mock
    private AiTaskRepository aiTaskRepository;
    @Mock
    private RagRecordRepository ragRecordRepository;
    @Mock
    private QualityMetricRepository qualityMetricRepository;
    @Mock
    private ErrorCorrectionRecordRepository errorCorrectionRecordRepository;
    @Mock
    private AcceptanceChecklistItemRepository acceptanceChecklistItemRepository;
    @Mock
    private AiTaskDispatchService aiTaskDispatchService;

    @InjectMocks
    private MilestoneService milestoneService;

    @org.junit.jupiter.api.AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listTasksByStatusShouldRejectInvalidStatus() {
        assertThrows(IllegalArgumentException.class, () -> milestoneService.listTasksByStatus("UNKNOWN"));
    }

    @Test
    void listTasksByStatusShouldNormalizeAndQueryRepository() {
        when(aiTaskRepository.findByTaskStatusOrderByUpdatedAtDesc("FAILED")).thenReturn(List.of());

        assertEquals(0, milestoneService.listTasksByStatus("failed").size());
        verify(aiTaskRepository).findByTaskStatusOrderByUpdatedAtDesc("FAILED");
    }

    @Test
    void listAcceptanceWithoutStepShouldReturnOrderedChecklist() {
        AcceptanceChecklistItem item = new AcceptanceChecklistItem();
        item.setStepNo(1);
        item.setItemName("item");
        when(acceptanceChecklistItemRepository.findAllByOrderByStepNoAscIdAsc()).thenReturn(List.of(item));

        List<AcceptanceChecklistItem> result = milestoneService.listAcceptance(null);
        assertEquals(1, result.size());
        verify(acceptanceChecklistItemRepository).findAllByOrderByStepNoAscIdAsc();
    }

    @Test
    void listAcceptanceShouldRejectOutOfRangeStep() {
        assertThrows(IllegalArgumentException.class, () -> milestoneService.listAcceptance(13));
    }

    @Test
    void publishNoticeShouldRequireAdminRole() {
        mockCurrentUser("student-user", UserRole.STUDENT);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> milestoneService.publishNotice("公告", "内容", "STUDENT")
        );
        assertEquals("Only admin can publish system notices", ex.getMessage());
        verify(systemNoticeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void publishNoticeShouldRejectInvalidAudienceRole() {
        mockCurrentUser("admin-user", UserRole.ADMIN);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> milestoneService.publishNotice("公告", "内容", "UNKNOWN")
        );
        assertTrue(ex.getMessage().contains("Invalid audienceRole"));
        verify(systemNoticeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void recordRagResultShouldRequireEnterpriseRole() {
        mockCurrentUser("student-user", UserRole.STUDENT);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> milestoneService.recordRagResult("query", "context", 0.8d, 0.9d)
        );
        assertEquals("Only enterprise can record RAG result", ex.getMessage());
    }

    @Test
    void recordRagResultShouldRejectOutOfRangeConfidence() {
        mockCurrentUser("enterprise-user", UserRole.ENTERPRISE);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> milestoneService.recordRagResult("query", "context", 0.8d, 1.2d)
        );
        assertEquals("confidence must be in [0,1]", ex.getMessage());
    }

    @Test
    void interviewFeedbackShouldRequireNotifiedStatus() {
        UserAccount enterprise = mockCurrentUser("enterprise-user", UserRole.ENTERPRISE);
        enterprise.setEnterpriseName("ACME");

        StudentApplication application = new StudentApplication();
        application.setId(1L);
        application.setJobId(11L);
        application.setStatus(ApplicationStatus.APPLIED);

        com.ifreelife.carrertry.entity.JobPosting jobPosting = new com.ifreelife.carrertry.entity.JobPosting();
        jobPosting.setId(11L);
        jobPosting.setEnterpriseName("ACME");

        when(studentApplicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(jobPostingRepository.findById(11L)).thenReturn(Optional.of(jobPosting));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> milestoneService.interviewFeedback(1L, "反馈", true)
        );
        assertEquals("Interview feedback can only be submitted after interview notice", ex.getMessage());
    }

    private UserAccount mockCurrentUser(String username, UserRole role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(username, "N/A"));
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setRole(role);
        when(userAccountRepository.findByUsername(username)).thenReturn(Optional.of(user));
        return user;
    }
}
