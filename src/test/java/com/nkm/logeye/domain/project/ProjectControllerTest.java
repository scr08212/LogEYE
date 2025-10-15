package com.nkm.logeye.domain.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.nkm.logeye.domain.account.Account;
import com.nkm.logeye.domain.account.AccountRepository;
import com.nkm.logeye.domain.account.AccountStatus;
import com.nkm.logeye.domain.auth.dto.LoginRequestDto;
import com.nkm.logeye.domain.project.dto.ProjectCreateRequestDto;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectControllerTest {
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

    private Account accountA;
    private Account accountB;
    private Project projectA;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
        accountRepository.deleteAll();

        accountA = accountRepository.save(Account.builder()
                .email("accountA@example.com")
                .password(passwordEncoder.encode("password"))
                .name("AccountA")
                .status(AccountStatus.ACTIVE)
                .build());

        accountB = accountRepository.save(Account.builder()
                .email("accountB@example.com")
                .password(passwordEncoder.encode("password"))
                .name("AccountB")
                .status(AccountStatus.ACTIVE)
                .build());

        projectA = projectRepository.save(Project.builder()
                .name("Project Of A")
                .account(accountA)
                .apiKey("testApiKey")
                .status(ProjectStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("E2E 테스트: 실제 로그인으로 토큰을 받은 후 프로젝트 생성")
    public void createProject_E2E_success() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto(accountA.getEmail(), "password");
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        String accessToken = JsonPath.read(responseBody, "$.data.accessToken");

        String projectName = "E2E Test Project";
        ProjectCreateRequestDto createRequestDto  = new ProjectCreateRequestDto(projectName);
        mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value(projectName));
    }

    @Test
    @DisplayName("프로젝트 생성 성공")
    @WithMockUser(username = "accountA@example.com", roles = "USER")
    public void createProject_success() throws Exception {
        ProjectCreateRequestDto requestDto = new ProjectCreateRequestDto("Test Project");

        mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data.name").value("Test Project"))
                .andExpect(jsonPath("$.data.apiKey").isNotEmpty())
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty())
                .andExpect(status().isCreated());

    }

    @ParameterizedTest
    @DisplayName("프로젝트 생성 실패 - 유효하지 않는 데이터 - 프로젝트명이 비어있음")
    @ValueSource(strings = {"", " "})
    @WithMockUser(username = "accountA@example.com", roles = "USER")
    public void createProject_fail_blank_name(String invalidName) throws Exception {
        ProjectCreateRequestDto requestDto = new ProjectCreateRequestDto(invalidName);
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - 유효하지 않는 데이터 - 프로젝트명이 100자 초과")
    @WithMockUser(username = "accountA@example.com", roles = "USER")
    public void createProject_fail_invalid_exceed_length_limit() throws Exception {
        String longName = "a".repeat(101);
        ProjectCreateRequestDto requestDto = new ProjectCreateRequestDto(longName);
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - 인증되지 않은 사용자")
    public void createProject_fail_unauthorized_account() throws Exception {
        ProjectCreateRequestDto requestDto = new ProjectCreateRequestDto("Test Project");
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("프로젝트 전체 조회 성공 - 프로젝트 보유 사용자")
    @WithMockUser(username = "accountA@example.com", roles = "USER")
    public void getAllProject_success() throws Exception {
        mockMvc.perform(get("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data.projects").isArray())
                .andExpect(jsonPath("$.data.projects", hasSize(1)))
                .andExpect(jsonPath("$.data.projects[0].name").value(projectA.getName()))
                .andExpect(jsonPath("$.data.projects[0].accountId").value(accountA.getId()));
    }

    @Test
    @DisplayName("프로젝트 전체 조회 성공 - 프로젝트 미보유 사용자")
    @WithMockUser(username = "accountB@example.com", roles = "USER")
    public void getAllProject_success_without_projects() throws Exception {
        mockMvc.perform(get("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data.projects").isArray())
                .andExpect(jsonPath("$.data.projects", hasSize(0)));
    }

    @Test
    @DisplayName("프로젝트 전체 조회 실패 - 인증되지 않은 사용자")
    public void getAllProject_fail_unauthorized_account() throws Exception {
        mockMvc.perform(get("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("특정 프로젝트 조회 성공")
    @WithMockUser(username = "accountA@example.com", roles = "USER")
    public void getProject_success() throws Exception {
        Long projectId = projectA.getId();

        mockMvc.perform(get("/api/v1/projects/" + projectId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data.id").value(projectId))
                .andExpect(jsonPath("$.data.accountId").value(accountA.getId()))
                .andExpect(jsonPath("$.data.name").value(projectA.getName()))
                .andExpect(jsonPath("$.data.apiKey").value(projectA.getApiKey()));
    }

    @Test
    @DisplayName("특정 프로젝트 조회 실패 - 존재하지 않는 프로젝트")
    @WithMockUser(username = "accountA@example.com", roles = "USER")
    public void getProject_fail_nonexistent_id() throws Exception {
        Long projectId = 500L;

        mockMvc.perform(get("/api/v1/projects/" + projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("특정 프로젝트 조회 실패 - 소유한 프로젝트가 아님")
    @WithMockUser(username = "accountB@example.com", roles = "USER")
    public void getProject_fail_not_owned_project() throws Exception {
        Long projectId = projectA.getId();

        mockMvc.perform(get("/api/v1/projects/" + projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("특정 프로젝트 조회 실패 - 인증되지 않은 사용자")
    public void getProject_fail_unauthorized_account() throws Exception {
        Long projectId = projectA.getId();

        mockMvc.perform(get("/api/v1/projects/" + projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("프로젝트 삭제 성공")
    @WithMockUser(username = "accountA@example.com", roles = "USER")
    public void deleteProject_success() throws Exception {
        Long projectId = projectA.getId();

        mockMvc.perform(delete(String.format("/api/v1/projects/%s", projectId))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        boolean isExist = projectRepository.existsById(projectId);
        assertThat(isExist).isFalse();
    }

    @Test
    @DisplayName("프로젝트 삭제 실패 - 소유한 프로젝트가 아님")
    @WithMockUser(username = "accountB@example.com", roles = "USER")
    public void deleteProject_fail_not_owned_project() throws Exception {
        Long projectId = projectA.getId();

        mockMvc.perform(delete(String.format("/api/v1/projects/%s", projectId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("프로젝트 삭제 실패 - 인증되지 않은 사용자")
    public void deleteProject_fail_unauthorized_account() throws Exception {
        Long projectId = projectA.getId();

        mockMvc.perform(delete(String.format("/api/v1/projects/%s", projectId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}