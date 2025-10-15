package com.nkm.logeye.domain.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nkm.logeye.domain.account.Account;
import com.nkm.logeye.domain.account.AccountRepository;
import com.nkm.logeye.domain.account.AccountStatus;
import com.nkm.logeye.domain.auth.dto.LoginRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext context;

    @Test
    @DisplayName("임시 보호된 API - 토큰 없이 접근 실패")
    void protectedApi_fail_no_token() throws Exception {
        mockMvc.perform(get("/api/v1/test/protected"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @BeforeEach
    void setUp() {
        // UTF-8 인코딩 필터 추가 (한글 깨짐 방지)
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .build();

        // 테스트에 사용할 사용자 미리 저장
        accountRepository.deleteAll();
        Account testUser = Account.builder()
                .email("user@gmail.com")
                .password(passwordEncoder.encode("12345678"))
                .name("Test User")
                .status(AccountStatus.ACTIVE)
                .build();
        accountRepository.save(testUser);
    }

    @Test
    @DisplayName("로그인 API 성공")
    void loginApi_success() throws Exception {
        // given
        LoginRequestDto requestDto = new LoginRequestDto("user@gmail.com", "12345678");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("로그인 API 실패 - 잘못된 비밀번호")
    void loginApi_fail_wrong_password() throws Exception {
        // given
        LoginRequestDto requestDto = new LoginRequestDto("user@gmail.com", "wrong password");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized()); // SecurityConfig와 EntryPoint에 의해 401 반환
    }

}