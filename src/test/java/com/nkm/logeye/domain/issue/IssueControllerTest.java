package com.nkm.logeye.domain.issue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nkm.logeye.domain.account.Account;
import com.nkm.logeye.domain.account.AccountRepository;
import com.nkm.logeye.domain.account.AccountStatus;
import com.nkm.logeye.domain.ai.AIAnalysisClient;
import com.nkm.logeye.domain.ai.dto.AIAnalysisResponseDto;
import com.nkm.logeye.domain.issue.dto.IssueStatusUpdateRequestDto;
import com.nkm.logeye.domain.project.Project;
import com.nkm.logeye.domain.project.ProjectRepository;
import com.nkm.logeye.domain.project.ProjectStatus;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.ZonedDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private IssueEventRepository issueEventRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AIAnalysisClient aiAnalysisClient;

    private Account owner;
    private Project project;
    private Issue issue, resolvedIssue, ignoredIssue;
    private IssueEvent issueEvent;
    private String mockStackTrace;


    @BeforeEach
    void setUp() {
        owner = accountRepository.save(Account.builder()
                .email("owner@test.com")
                .password(passwordEncoder.encode("password"))
                .name("owner")
                .status(AccountStatus.ACTIVE)
                .build());

        project = projectRepository.save(Project.builder()
                .account(owner)
                .name("Test Project")
                .apiKey("test-api-key")
                .status(ProjectStatus.ACTIVE)
                .build());

        mockStackTrace = """
                        java.lang.NullPointerException: Cannot invoke "String.length()" because "s" is null
                        \tat com.nkm.logeye.service.MyService.doSomething(MyService.java:10)
                        \tat com.nkm.logeye.controller.MyController.handle(MyController.java:20)
                        """;

        issue = issueRepository.save(Issue.builder()
                .project(project)
                .message("This is UNHANDLED")
                .status(IssueStatus.UNHANDLED)
                .level(IssueLevel.ERROR)
                .stackTrace(mockStackTrace)
                .eventCount(10L)
                .fingerprint("test-fingerprint-1234")
                .lastSeen(ZonedDateTime.now())
                .build());

        resolvedIssue = issueRepository.save(Issue.builder()
                .project(project)
                .message("This is RESOLVED")
                .status(IssueStatus.RESOLVED)
                .level(IssueLevel.ERROR)
                .stackTrace(mockStackTrace)
                .eventCount(5L)
                .fingerprint("test-fingerprint-12345")
                .lastSeen(ZonedDateTime.now())
                .build());

        ignoredIssue = issueRepository.save(Issue.builder()
                .project(project)
                .message("This is IGNORED")
                .status(IssueStatus.IGNORED)
                .level(IssueLevel.ERROR)
                .stackTrace(mockStackTrace)
                .eventCount(20L)
                .fingerprint("test-fingerprint-123456")
                .lastSeen(ZonedDateTime.now())
                .build());

        issueEvent = issueEventRepository.save(IssueEvent.builder()
                .issue(issue)
                .occurredAt(ZonedDateTime.now())
                .contextData("{\"appVersion\":\"1.0.0\", \"environment\":\"production\", \"key\":\"value\"}")
                .build());
    }

    @Test
    @DisplayName("이슈 목록 조회 API 성공")
    @WithMockUser(username = "owner@test.com", roles = "USER")
    void getIssuesByProject_success() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{projectId}/issues", project.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(issue.getId()));
    }

    @Test
    @DisplayName("이슈 상세 조회 API 성공")
    @WithMockUser(username = "owner@test.com", roles = "USER")
    void getIssueDetails_success() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{projectId}/issues/{issueId}", project.getId(), issue.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(issue.getId()))
                .andExpect(jsonPath("$.data.message").value("This is UNHANDLED"));
    }

    @Test
    @DisplayName("이슈 상세 조회 API 실패 - 다른 사용자 접근")
    @WithMockUser(username = "other@test.com", roles = "USER")
    void getIssueDetails_fail_notOwner() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{projectId}/issues/{issueId}", project.getId(), issue.getId()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("이슈 상태 변경 API 성공")
    @WithMockUser(username = "owner@test.com", roles = "USER")
    void updateIssueStatus_success() throws Exception {
        IssueStatusUpdateRequestDto requestDto = new IssueStatusUpdateRequestDto(IssueStatus.RESOLVED);

        mockMvc.perform(patch("/api/v1/projects/{projectId}/issues/{issueId}/status", project.getId(), issue.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("이슈 목록 조회 API 성공 - UNHANDLED 상태 필터링")
    @WithMockUser(username = "owner@test.com", roles = "USER")
    void getIssues_withStatusFilter_success() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{projectId}/issues", project.getId())
                        .param("status", "UNHANDLED")) // status=UNHANDLED 파라미터 추가
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1)) // UNHANDLED 상태인 이슈는 1개
                .andExpect(jsonPath("$.data.content[0].message").value("This is UNHANDLED"));
    }

    @Test
    @DisplayName("이슈 목록 조회 API 성공 - eventCount 내림차순 정렬")
    @WithMockUser(username = "owner@test.com", roles = "USER")
    void getIssues_withSorting_success() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{projectId}/issues", project.getId())
                        .param("sort", "eventCount,desc")) // sort=eventCount,desc 파라미터 추가
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3)) // 필터링 없으므로 총 3개
                .andExpect(jsonPath("$.data.content[0].message").value("This is IGNORED")) // eventCount가 20으로 가장 높음
                .andExpect(jsonPath("$.data.content[1].message").value("This is UNHANDLED")) // eventCount가 10
                .andExpect(jsonPath("$.data.content[2].message").value("This is RESOLVED")); // eventCount가 5로 가장 낮음
    }

    @Test
    @DisplayName("특정 이슈의 이벤트 목록 조회 API 성공")
    @WithMockUser(username = "owner@test.com", roles = "USER")
    void getIssueEvents_success() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{projectId}/issues/{issueId}/events", project.getId(), issue.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(issueEvent.getId()));
    }

    @Test
    @DisplayName("특정 이슈의 이벤트 목록 조회 API 실패 - 다른 사용자 접근")
    @WithMockUser(username = "other@test.com", roles = "USER")
    void getIssueEvents_fail_notOwner() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{projectId}/issues/{issueId}/events", project.getId(), issue.getId()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("AI 이슈 분석 API 성공")
    @WithMockUser(username = "owner@test.com", roles = "USER")
    void analysisIssue_success() throws Exception {
        // given
        AIAnalysisResponseDto responseDto = new AIAnalysisResponseDto("NPE 발생", "null 체크 추가");
        when(aiAnalysisClient.analyze(anyString())).thenReturn(responseDto);
        // when
        mockMvc.perform(post("/api/v1/projects/{projectId}/issues/{issueId}/analyze", project.getId(), issue.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estimated_cause").value("NPE 발생"))
                .andExpect(jsonPath("$.data.solution_suggestion").value("null 체크 추가"))
                .andExpect(jsonPath("$.data.impacted_file").value("MyService.java:10"));

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiAnalysisClient).analyze(promptCaptor.capture());

        String generatedPrompt = promptCaptor.getValue();
        assertThat(generatedPrompt).contains("This is UNHANDLED"); // message 포함 확인
        assertThat(generatedPrompt).contains("MyService.java:10"); // stackTrace 포함 확인
        assertThat(generatedPrompt).contains("appVersion\":\"1.0.0"); // contextData 포함 확인
    }

    @Test
    @DisplayName("AI 이슈 분석 API 실패 - 다른 사용자 접근")
    @WithMockUser(username = "other@test.com", roles = "USER")
    void analysisIssue_fail_notOwner() throws Exception {
        mockMvc.perform(post("/api/v1/projects/{projectId}/issues/{issueId}/analyze", project.getId(), issue.getId()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}