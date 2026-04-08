package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.entity.AcceptanceChecklistItem;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
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
}
