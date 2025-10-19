package com.nkm.logeye.domain.issue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nkm.logeye.domain.account.Account;
import com.nkm.logeye.domain.account.AccountRepository;
import com.nkm.logeye.domain.account.AccountStatus;
import com.nkm.logeye.domain.issue.dto.IssueStatusUpdateRequestDto;
import com.nkm.logeye.domain.project.Project;
import com.nkm.logeye.domain.project.ProjectRepository;
import com.nkm.logeye.domain.project.ProjectStatus;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private PasswordEncoder passwordEncoder;

    private Account owner;
    private Project project;
    private Issue issue;

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

        issue = issueRepository.save(Issue.builder()
                .project(project)
                .message("Initial Issue")
                .status(IssueStatus.UNHANDLED)
                .level(IssueLevel.ERROR)
                .eventCount(10L)
                .fingerprint("test-fingerprint-1234")
                .lastSeen(ZonedDateTime.now())
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
        mockMvc.perform(get("/api/v1/issues/{issueId}", issue.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(issue.getId()))
                .andExpect(jsonPath("$.data.message").value("Initial Issue"));
    }

    @Test
    @DisplayName("이슈 상세 조회 API 실패 - 다른 사용자 접근")
    @WithMockUser(username = "other@test.com", roles = "USER")
    void getIssueDetails_fail_notOwner() throws Exception {
        mockMvc.perform(get("/api/v1/issues/{issueId}", issue.getId()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("이슈 상태 변경 API 성공")
    @WithMockUser(username = "owner@test.com", roles = "USER")
    void updateIssueStatus_success() throws Exception {
        IssueStatusUpdateRequestDto requestDto = new IssueStatusUpdateRequestDto(IssueStatus.RESOLVED);

        mockMvc.perform(patch("/api/v1/issues/{issueId}/status", issue.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}