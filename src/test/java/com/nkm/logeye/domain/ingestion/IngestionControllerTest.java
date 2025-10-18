package com.nkm.logeye.domain.ingestion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nkm.logeye.domain.account.Account;
import com.nkm.logeye.domain.account.AccountRepository;
import com.nkm.logeye.domain.account.AccountStatus;
import com.nkm.logeye.domain.ingestion.dto.IssueEventRequestDto;
import com.nkm.logeye.domain.project.Project;
import com.nkm.logeye.domain.project.ProjectRepository;
import com.nkm.logeye.domain.project.ProjectStatus;
import com.nkm.logeye.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IngestionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private IssueEventRepository issueEventRepository;

    private Project projectA;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        projectRepository.deleteAll();
        issueRepository.deleteAll();
        issueEventRepository.deleteAll();

        Account accountA = accountRepository.save(Account.builder()
                .email("accountA@example.com")
                .password(passwordEncoder.encode("password"))
                .name("AccountA")
                .status(AccountStatus.ACTIVE)
                .build());

        projectA = projectRepository.save(Project.builder()
                .name("Project Of A")
                .account(accountA)
                .apiKey("test-api-key-for-project-a") // 예측 가능한 API Key 사용
                .status(ProjectStatus.ACTIVE)
                .build());

    }

    @Test
    @DisplayName("로그 수집 성공 - 유효한 API Key")
    void ingestLog_success_withValidApiKey() throws Exception {
        // given
        IssueEventRequestDto requestDto = new IssueEventRequestDto(
                "NullPointerException occurred",
                "at com.example.service.MyService.doSomething(MyService.java:10)",
                ZonedDateTime.now(),
                "production",
                "1.0.0",
                "{\"userId\":\"user-123\"}"
        );

        // when & then
        mockMvc.perform(post("/api/v1/logs")
                        .header("X-LOGEYE-API-KEY", projectA.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("로그 수집 실패 - 유효하지 않은 API Key")
    void ingestLog_fail_withInvalidApiKey() throws Exception {
        // given
        String invalidApiKey = "invalid-api-key-12345";
        IssueEventRequestDto requestDto = new IssueEventRequestDto(
                "Test Error", null, ZonedDateTime.now(), null, null, null);

        // when & then
        mockMvc.perform(post("/api/v1/logs")
                        .header("X-LOGEYE-API-KEY", invalidApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isForbidden()) // INVALID_API_KEY ErrorCode가 403을 반환하도록 설정했으므로
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.INVALID_API_KEY.getCode()));
    }

    @Test
    @DisplayName("로그 수집 실패 - 잘못된 DTO 데이터 (필수 필드 누락)")
    void ingestLog_fail_withInvalidDto() throws Exception {
        // given
        // message 필드가 null인 잘못된 DTO
        IssueEventRequestDto requestDto = new IssueEventRequestDto(
                null, null, ZonedDateTime.now(), null, null, null);

        // when & then
        mockMvc.perform(post("/api/v1/logs")
                        .header("X-LOGEYE-API-KEY", projectA.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.data.message").value("에러 메세지는 비어 있을 수 없습니다."));
    }
}