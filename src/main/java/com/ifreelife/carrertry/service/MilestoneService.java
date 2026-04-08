package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.entity.*;
import com.ifreelife.carrertry.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MilestoneService {
    private static final int STUDENT_HOME_PAGE_SIZE = 15;
    private static final int MAX_ACCEPTANCE_STEP = 12;
    private static final Set<String> AI_TASK_STATUSES = Set.of("QUEUED", "EXECUTING", "SUCCESS", "FAILED");
    private static final List<String> MBTI_TYPES = List.of(
        "INTJ", "INTP", "ENTJ", "ENTP", "INFJ", "INFP", "ENFJ", "ENFP",
        "ISTJ", "ISFJ", "ESTJ", "ESFJ", "ISTP", "ISFP", "ESTP", "ESFP"
    );

    private final UserAccountRepository userAccountRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ResumeRecordRepository resumeRecordRepository;
    private final JobPostingRepository jobPostingRepository;
    private final StudentApplicationRepository studentApplicationRepository;
    private final SchoolFeedbackRepository schoolFeedbackRepository;
    private final TeacherMentorRepository teacherMentorRepository;
    private final CareerPlanRepository careerPlanRepository;
    private final SystemNoticeRepository systemNoticeRepository;
    private final AchievementDefinitionRepository achievementDefinitionRepository;
    private final StudentAchievementRepository studentAchievementRepository;
    private final AiTaskRepository aiTaskRepository;
    private final RagRecordRepository ragRecordRepository;
    private final QualityMetricRepository qualityMetricRepository;
    private final ErrorCorrectionRecordRepository errorCorrectionRecordRepository;
    private final AcceptanceChecklistItemRepository acceptanceChecklistItemRepository;
    private final AiTaskDispatchService aiTaskDispatchService;

    @Transactional
    public StudentProfile completeOnboarding(String techStack, String capabilityInfo, String mbtiType, boolean commitmentAgreed) {
        UserAccount student = currentUser();
        if (student.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Only student can complete onboarding");
        }
        if (!MBTI_TYPES.contains(mbtiType.toUpperCase())) {
            throw new IllegalArgumentException("Invalid MBTI type");
        }
        if (!commitmentAgreed) {
            throw new IllegalArgumentException("Commitment must be agreed");
        }
        StudentProfile profile = studentProfileRepository.findByStudentUsername(student.getUsername())
            .orElseGet(StudentProfile::new);
        profile.setStudentUsername(student.getUsername());
        profile.setStudentName(student.getDisplayName());
        profile.setTechStack(techStack.trim());
        profile.setCapabilityInfo(capabilityInfo.trim());
        profile.setMbtiType(mbtiType.toUpperCase());
        profile.setCommitmentAgreed(true);
        profile.setOnboardingCompleted(true);
        profile.setPortraitVector12(generatePortraitVector(techStack, capabilityInfo));
        profile.setPortraitTags(generateTags(profile));
        profile.setAiSummary(generateAiSummary(profile));
        profile.setUpdatedAt(LocalDateTime.now());
        StudentProfile saved = studentProfileRepository.save(profile);
        student.setOnboardingCompleted(true);
        userAccountRepository.save(student);
        grantAchievement(student.getUsername(), "ACH_ONBOARDING");
        return saved;
    }

    public StudentProfile myProfile() {
        UserAccount user = currentUser();
        return studentProfileRepository.findByStudentUsername(user.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
    }

    @Transactional
    public UserAccount updateStudentBaseInfo(String displayName, String phone, String major) {
        UserAccount user = currentUser();
        if (user.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Only student can edit student center");
        }
        user.setDisplayName(displayName.trim());
        user.setPhone(phone.trim());
        user.setMajor(major.trim());
        return userAccountRepository.save(user);
    }

    @Transactional
    public ResumeRecord saveResume(String fileName, String rawContent) {
        UserAccount user = currentUser();
        if (user.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Only student can manage resume");
        }
        ResumeRecord record = new ResumeRecord();
        record.setStudentUsername(user.getUsername());
        record.setFileName(fileName.trim());
        record.setRawContent(rawContent.trim());
        record.setAiSuggestion("建议补充项目结果量化、核心技术栈深度与业务影响。");
        record.setBeautifiedContent("【美化版】" + rawContent.trim());
        record.setUpdatedAt(LocalDateTime.now());
        grantAchievement(user.getUsername(), "ACH_RESUME_UPLOAD");
        return resumeRecordRepository.save(record);
    }

    public List<ResumeRecord> listMyResumes() {
        UserAccount user = currentUser();
        return resumeRecordRepository.findByStudentUsernameOrderByUpdatedAtDesc(user.getUsername());
    }

    public Page<JobPosting> studentHomeJobs(int page) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        return jobPostingRepository.findByStatus(JobStatus.APPROVED, PageRequest.of(page, STUDENT_HOME_PAGE_SIZE));
    }

    public boolean canStudentApply() {
        UserAccount user = currentUser();
        if (user.getRole() != UserRole.STUDENT) {
            return false;
        }
        return user.getOnboardingCompleted();
    }

    @Transactional
    public List<Map<String, Object>> jobMatchScores() {
        UserAccount user = currentUser();
        if (!Boolean.TRUE.equals(user.getOnboardingCompleted())) {
            throw new IllegalArgumentException("Complete first-login onboarding before portrait and matching");
        }
        List<JobPosting> jobs = jobPostingRepository.findByStatus(JobStatus.APPROVED);
        StudentProfile profile = studentProfileRepository.findByStudentUsername(user.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (JobPosting job : jobs) {
            int skill = scoreByContains(job.getSkills(), profile.getTechStack());
            int exp = scoreByContains(job.getExperienceRequirement(), profile.getCapabilityInfo());
            int edu = scoreByContains(job.getEducationRequirement(), profile.getCapabilityInfo());
            int growth = Math.min(100, 40 + (profile.getTechStack() == null ? 0 : profile.getTechStack().length() % 61));
            int total = (skill + exp + edu + growth) / 4;
            rows.add(Map.of(
                "jobId", job.getId(),
                "jobTitle", job.getTitle(),
                "skillScore", skill,
                "experienceScore", exp,
                "educationScore", edu,
                "growthScore", growth,
                "totalScore", total
            ));
        }
        rows.sort((a, b) -> Integer.compare((Integer) b.get("totalScore"), (Integer) a.get("totalScore")));
        return rows;
    }

    @Transactional
    public TeacherMentor createMentor(String name, String expertise, String phone, String availableTime, String location) {
        UserAccount school = currentUser();
        if (school.getRole() != UserRole.SCHOOL) {
            throw new IllegalArgumentException("Only school can manage mentors");
        }
        TeacherMentor mentor = new TeacherMentor();
        mentor.setSchoolName(requireSchoolName(school));
        mentor.setName(name.trim());
        mentor.setExpertise(expertise.trim());
        mentor.setPhone(phone.trim());
        mentor.setAvailableTime(availableTime.trim());
        mentor.setLocation(location.trim());
        return teacherMentorRepository.save(mentor);
    }

    public List<TeacherMentor> listMentors() {
        UserAccount school = currentUser();
        return teacherMentorRepository.findBySchoolNameOrderByIdDesc(requireSchoolName(school));
    }

    public List<Map<String, Object>> schoolStudentsWithProfile() {
        UserAccount school = currentUser();
        List<UserAccount> students = userAccountRepository.findByRoleAndSchoolName(UserRole.STUDENT, requireSchoolName(school));
        return students.stream().map(student -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("username", student.getUsername());
            row.put("displayName", student.getDisplayName());
            row.put("major", student.getMajor());
            row.put("onboardingCompleted", student.getOnboardingCompleted());
            studentProfileRepository.findByStudentUsername(student.getUsername()).ifPresent(profile -> {
                row.put("portraitTags", profile.getPortraitTags());
                row.put("aiSummary", profile.getAiSummary());
            });
            return row;
        }).toList();
    }

    @Transactional
    public StudentApplication interviewNotify(Long applicationId, String notice) {
        UserAccount enterprise = currentUser();
        if (enterprise.getRole() != UserRole.ENTERPRISE) {
            throw new IllegalArgumentException("Only enterprise can send interview notice");
        }
        StudentApplication application = studentApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        verifyEnterpriseOwnsApplication(application, enterprise);
        application.setStatus(ApplicationStatus.INTERVIEW_NOTIFIED);
        application.setInterviewNotice(notice.trim());
        studentApplicationRepository.save(application);
        SystemNotice noticeRecord = new SystemNotice();
        noticeRecord.setTitle("面试通知");
        noticeRecord.setContent(notice.trim());
        noticeRecord.setNoticeType("INTERVIEW");
        noticeRecord.setAudienceRole("STUDENT");
        systemNoticeRepository.save(noticeRecord);
        return application;
    }

    @Transactional
    public StudentApplication interviewFeedback(Long applicationId, String feedback, boolean passed) {
        UserAccount enterprise = currentUser();
        if (enterprise.getRole() != UserRole.ENTERPRISE) {
            throw new IllegalArgumentException("Only enterprise can submit interview feedback");
        }
        StudentApplication application = studentApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        verifyEnterpriseOwnsApplication(application, enterprise);
        application.setInterviewFeedback(feedback.trim());
        application.setStatus(passed ? ApplicationStatus.INTERVIEW_COMPLETED : ApplicationStatus.REJECTED);
        if (passed) {
            grantAchievement(application.getStudentUsername(), "ACH_INTERVIEW_PASS");
        }
        return studentApplicationRepository.save(application);
    }

    public List<StudentApplication> enterpriseApplications() {
        UserAccount enterprise = currentUser();
        if (enterprise.getRole() != UserRole.ENTERPRISE) {
            throw new IllegalArgumentException("Only enterprise can view applications");
        }
        List<Long> ownedJobIds = ownedJobIds(enterprise.getEnterpriseName());
        if (ownedJobIds.isEmpty()) {
            return Collections.emptyList();
        }
        return studentApplicationRepository.findByJobIdInOrderByAppliedAtDesc(ownedJobIds);
    }

    public List<StudentApplication> intelligentScreening(String keyword) {
        UserAccount enterprise = currentUser();
        if (enterprise.getRole() != UserRole.ENTERPRISE) {
            throw new IllegalArgumentException("Only enterprise can view applications");
        }
        List<Long> ownedJobIds = ownedJobIds(enterprise.getEnterpriseName());
        if (ownedJobIds.isEmpty()) {
            return Collections.emptyList();
        }
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        if (key.isEmpty()) {
            return studentApplicationRepository.findByJobIdInOrderByAppliedAtDesc(ownedJobIds);
        }
        return studentApplicationRepository.searchByJobIdsAndKeyword(ownedJobIds, key);
    }

    @Transactional
    public CareerPlan saveCareerPlan(String city, String career, Integer progressPercent, String dynamicAdjustment) {
        UserAccount student = currentUser();
        if (student.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Only student can set career plan");
        }
        CareerPlan plan = careerPlanRepository.findByStudentUsername(student.getUsername()).orElseGet(CareerPlan::new);
        plan.setStudentUsername(student.getUsername());
        plan.setTargetCity(city.trim());
        plan.setTargetCareer(career.trim());
        int score = Math.min(100, Math.max(0, 45 + (city.length() * 3 + career.length() * 2) % 56));
        plan.setMatchPercent(score);
        plan.setCareerPlanning(score >= 70 ? "匹配度达标，进入强化培养路径。" : "匹配度不足70%，建议补齐关键能力后再冲刺目标。");
        plan.setTrainingPlan("阶段1夯实基础；阶段2项目实战；阶段3模拟面试与反馈闭环。");
        plan.setProgressPercent(Math.max(0, Math.min(100, progressPercent)));
        plan.setDynamicAdjustment(dynamicAdjustment == null ? "" : dynamicAdjustment.trim());
        plan.setUpdatedAt(LocalDateTime.now());
        grantAchievement(student.getUsername(), "ACH_PLAN_CREATED");
        return careerPlanRepository.save(plan);
    }

    public CareerPlan myCareerPlan() {
        UserAccount student = currentUser();
        return careerPlanRepository.findByStudentUsername(student.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("Career plan not found"));
    }

    public byte[] downloadCareerPlanPdf() {
        CareerPlan plan = myCareerPlan();
        String text = "Career Plan\nStudent: " + plan.getStudentUsername()
            + "\nCity: " + plan.getTargetCity()
            + "\nCareer: " + plan.getTargetCareer()
            + "\nMatch: " + plan.getMatchPercent() + "%\n"
            + "Planning: " + plan.getCareerPlanning() + "\nTraining: " + plan.getTrainingPlan();
        return simplePdf(text);
    }

    @Transactional
    public SystemNotice publishNotice(String title, String content, String audienceRole) {
        currentUser();
        SystemNotice notice = new SystemNotice();
        notice.setTitle(title.trim());
        notice.setContent(content.trim());
        notice.setNoticeType("SYSTEM");
        notice.setAudienceRole(audienceRole.trim().toUpperCase());
        return systemNoticeRepository.save(notice);
    }

    public List<SystemNotice> noticesForCurrentUser() {
        UserAccount user = currentUser();
        return systemNoticeRepository.findByAudienceRoleOrderByCreatedAtDesc(user.getRole().name());
    }

    @Transactional
    public void ensureAchievementDefinitions() {
        List<String[]> required = new ArrayList<>(List.of(
            new String[]{"ACH_ONBOARDING", "首次链路完成", "完成首次登录必填链路", "首次链路完成"},
            new String[]{"ACH_RESUME_UPLOAD", "简历上传", "首次上传简历", "上传简历"},
            new String[]{"ACH_INTERVIEW_PASS", "面试通过", "首次面试通过", "面试反馈通过"},
            new String[]{"ACH_PLAN_CREATED", "规划制定", "首次完成职业规划", "提交职业规划"}
        ));
        for (int i = 1; i <= 26; i++) {
            required.add(new String[]{
                "ACH_EXTRA_" + String.format("%02d", i),
                "拓展成就" + i,
                "系统拓展成就项" + i,
                "系统事件触发"
            });
        }
        for (String[] definitionData : required) {
            String code = definitionData[0];
            if (!achievementDefinitionRepository.existsByCode(code)) {
                AchievementDefinition definition = new AchievementDefinition();
                definition.setCode(code);
                definition.setName(definitionData[1]);
                definition.setDescription(definitionData[2]);
                definition.setTriggerCondition(definitionData[3]);
                achievementDefinitionRepository.save(definition);
            }
        }
    }

    public List<StudentAchievement> myAchievements() {
        UserAccount user = currentUser();
        return studentAchievementRepository.findByStudentUsernameOrderByAchievedAtDesc(user.getUsername());
    }

    public List<AiTask> listTasksByStatus(String status) {
        if (status == null || status.isBlank()) {
            return aiTaskRepository.findAll();
        }
        String normalizedStatus = status.trim().toUpperCase();
        if (!AI_TASK_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid task status. Allowed values: QUEUED, EXECUTING, SUCCESS, FAILED");
        }
        return aiTaskRepository.findByTaskStatusOrderByUpdatedAtDesc(normalizedStatus);
    }

    @Transactional
    public AiTask retryFailedTask(Long taskId) {
        AiTask task = aiTaskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        if (!"FAILED".equalsIgnoreCase(task.getTaskStatus())) {
            throw new IllegalArgumentException("Only FAILED task can retry");
        }
        int nextRetryCount = Optional.ofNullable(task.getRetryCount()).orElse(0) + 1;
        LocalDateTime queuedAt = LocalDateTime.now();
        if (aiTaskDispatchService.dispatchRetry(task.getId(), task.getTaskName(), nextRetryCount, queuedAt)) {
            task.setTaskStatus("QUEUED");
            task.setRetryCount(nextRetryCount);
            task.setErrorMessage(null);
            task.setUpdatedAt(queuedAt);
        } else {
            task.setTaskStatus("FAILED");
            task.setErrorMessage("RabbitMQ dispatch failed, please retry later");
            task.setUpdatedAt(LocalDateTime.now());
        }
        return aiTaskRepository.save(task);
    }

    @Transactional
    public RagRecord recordRagResult(String query, String context, Double qualityScore, Double confidence) {
        RagRecord record = new RagRecord();
        record.setQueryText(query.trim());
        record.setRetrievedContext(context.trim());
        record.setQualityScore(qualityScore);
        record.setConfidence(confidence);
        record.setReleased(confidence >= 0.7d);
        RagRecord saved = ragRecordRepository.save(record);
        QualityMetric metric = new QualityMetric();
        metric.setMetricName("rag_quality_score");
        metric.setMetricValue(qualityScore);
        qualityMetricRepository.save(metric);
        return saved;
    }

    public List<QualityMetric> latestQualityMetrics() {
        return qualityMetricRepository.findTop20ByOrderBySnapshotTimeDesc();
    }

    @Transactional
    public ErrorCorrectionRecord recordCorrection(String wrongPoint, String correctionAction, String closedLoopStatus) {
        UserAccount user = currentUser();
        ErrorCorrectionRecord record = new ErrorCorrectionRecord();
        record.setStudentUsername(user.getUsername());
        record.setWrongPoint(wrongPoint.trim());
        record.setCorrectionAction(correctionAction.trim());
        record.setClosedLoopStatus(closedLoopStatus.trim());
        return errorCorrectionRecordRepository.save(record);
    }

    public List<ErrorCorrectionRecord> myCorrectionRecords() {
        UserAccount user = currentUser();
        return errorCorrectionRecordRepository.findByStudentUsernameOrderByCreatedAtDesc(user.getUsername());
    }

    @Transactional
    public AcceptanceChecklistItem updateAcceptance(Integer stepNo, String itemName, boolean doneFlag, String note) {
        if (stepNo == null || stepNo < 1 || stepNo > MAX_ACCEPTANCE_STEP) {
            throw new IllegalArgumentException("stepNo must be in [1, " + MAX_ACCEPTANCE_STEP + "]");
        }
        AcceptanceChecklistItem item = new AcceptanceChecklistItem();
        item.setStepNo(stepNo);
        item.setItemName(itemName.trim());
        item.setDoneFlag(doneFlag);
        item.setNote(note == null ? "" : note.trim());
        item.setUpdatedAt(LocalDateTime.now());
        return acceptanceChecklistItemRepository.save(item);
    }

    public List<AcceptanceChecklistItem> listAcceptance(Integer stepNo) {
        if (stepNo == null) {
            return acceptanceChecklistItemRepository.findAllByOrderByStepNoAscIdAsc();
        }
        if (stepNo < 1 || stepNo > MAX_ACCEPTANCE_STEP) {
            throw new IllegalArgumentException("stepNo must be in [1, " + MAX_ACCEPTANCE_STEP + "]");
        }
        return acceptanceChecklistItemRepository.findByStepNoOrderByIdAsc(stepNo);
    }

    @Transactional
    public void refreshTeacherCommentSnapshot(StudentApplication application) {
        schoolFeedbackRepository.findTopByStudentNameOrderByCreatedAtDesc(application.getStudentName())
            .ifPresent(feedback -> application.setTeacherCommentSnapshot(feedback.getComment()));
        studentApplicationRepository.save(application);
    }

    private UserAccount currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
    }

    private String requireSchoolName(UserAccount school) {
        if (school.getSchoolName() == null || school.getSchoolName().isBlank()) {
            throw new IllegalArgumentException("Current school account has no school binding");
        }
        return school.getSchoolName().trim();
    }

    private List<Long> ownedJobIds(String enterpriseName) {
        if (enterpriseName == null || enterpriseName.isBlank()) {
            throw new IllegalArgumentException("Current enterprise account has no enterprise binding");
        }
        return jobPostingRepository.findByEnterpriseNameIgnoreCase(enterpriseName.trim())
            .stream()
            .map(JobPosting::getId)
            .toList();
    }

    private void verifyEnterpriseOwnsApplication(StudentApplication application, UserAccount enterprise) {
        if (enterprise.getEnterpriseName() == null || enterprise.getEnterpriseName().isBlank()) {
            throw new IllegalArgumentException("Current enterprise account has no enterprise binding");
        }
        JobPosting job = jobPostingRepository.findById(application.getJobId())
            .orElseThrow(() -> new IllegalArgumentException("Job not found: " + application.getJobId()));
        if (!enterprise.getEnterpriseName().trim().equalsIgnoreCase(job.getEnterpriseName())) {
            throw new IllegalArgumentException("No permission to operate this application");
        }
    }

    private void grantAchievement(String studentUsername, String code) {
        if (!studentAchievementRepository.existsByStudentUsernameAndAchievementCode(studentUsername, code)) {
            StudentAchievement achievement = new StudentAchievement();
            achievement.setStudentUsername(studentUsername);
            achievement.setAchievementCode(code);
            studentAchievementRepository.save(achievement);
        }
    }

    private int scoreByContains(String target, String source) {
        if (target == null || source == null || target.isBlank() || source.isBlank()) {
            return 40;
        }
        Set<String> parts = Arrays.stream(source.toLowerCase().split("[,，\\s]+"))
            .filter(s -> !s.isBlank())
            .collect(Collectors.toSet());
        long hit = parts.stream().filter(p -> target.toLowerCase().contains(p)).count();
        return (int) Math.min(100, 35 + hit * 20);
    }

    private String generatePortraitVector(String techStack, String capabilityInfo) {
        int seed = Math.abs((techStack + "|" + capabilityInfo).hashCode());
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            values.add(40 + ((seed + i * 17) % 61));
        }
        return values.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private String generateTags(StudentProfile profile) {
        List<String> tags = new ArrayList<>();
        if (profile.getTechStack() != null && profile.getTechStack().toLowerCase().contains("java")) tags.add("后端基础");
        if (profile.getTechStack() != null && profile.getTechStack().toLowerCase().contains("react")) tags.add("前端实践");
        if (profile.getCapabilityInfo() != null && profile.getCapabilityInfo().length() > 30) tags.add("表达完整");
        tags.add(profile.getMbtiType());
        return String.join(",", tags);
    }

    private String generateAiSummary(StudentProfile profile) {
        return "该同学技术栈为[" + Optional.ofNullable(profile.getTechStack()).orElse("未填写")
            + "]，MBTI为[" + Optional.ofNullable(profile.getMbtiType()).orElse("未知")
            + "]，建议聚焦岗位关键技能并持续复盘项目产出。";
    }

    private byte[] simplePdf(String text) {
        String escaped = text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
        String stream = "BT /F1 12 Tf 50 750 Td (" + escaped.replace("\n", ") Tj T* (") + ") Tj ET";
        List<String> objects = List.of(
            "<< /Type /Catalog /Pages 2 0 R >>",
            "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
            "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>",
            "<< /Length " + stream.getBytes(StandardCharsets.UTF_8).length + " >>\nstream\n" + stream + "\nendstream",
            "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"
        );
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        int byteOffset = "%PDF-1.4\n".getBytes(StandardCharsets.UTF_8).length;
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(byteOffset);
            String objectSection = (i + 1) + " 0 obj\n" + objects.get(i) + "\nendobj\n";
            pdf.append(objectSection);
            byteOffset += objectSection.getBytes(StandardCharsets.UTF_8).length;
        }
        int xrefOffset = byteOffset;
        pdf.append("xref\n")
            .append("0 ").append(objects.size() + 1).append("\n")
            .append("0000000000 65535 f \n");
        for (int i = 1; i < offsets.size(); i++) {
            pdf.append(String.format("%010d 00000 n %n", offsets.get(i)));
        }
        pdf.append("trailer\n")
            .append("<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n")
            .append("startxref\n")
            .append(xrefOffset).append("\n")
            .append("%%EOF");
        return pdf.toString().getBytes(StandardCharsets.UTF_8);
    }
}
