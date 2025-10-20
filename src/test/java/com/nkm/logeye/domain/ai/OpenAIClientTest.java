package com.nkm.logeye.domain.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nkm.logeye.domain.ai.dto.AnalysisResult;
import com.nkm.logeye.domain.issue.Issue;
import com.nkm.logeye.global.exception.AIAnalysisException;
import com.nkm.logeye.global.exception.ErrorCode;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class OpenAIClientTest {
    private static MockWebServer mockWebServer;

    @InjectMocks
    private OpenAIClient openAIClient;

    @Mock
    private AIProperties aiProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        when(aiProperties.getUrl()).thenReturn(baseUrl);
        openAIClient = new OpenAIClient(WebClient.builder(), aiProperties);
    }

    @Test
    @DisplayName("AI 분석 요청 성공")
    void analyze_success() throws Exception {
        // given
        AnalysisResult expectedResult = new AnalysisResult("Test Cause", "Test Solution", "TestFile.java");
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expectedResult))
                .addHeader("Content-Type", "application/json"));

        Issue mockIssue = Issue.builder().message("Test error message").build();

        // when
        AnalysisResult actualResult = openAIClient.analyze(mockIssue);

        // then
        assertThat(actualResult).isNotNull();
        assertThat(actualResult.estimatedCause()).isEqualTo("Test Cause");
    }

    @Test
    @DisplayName("AI 분석 실패 - 401 인증 오류")
    void analyze_fail_unauthorized() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));
        Issue mockIssue = Issue.builder().message("Test error message").build();

        // when & then
        assertThatThrownBy(() -> openAIClient.analyze(mockIssue))
                .isInstanceOf(AIAnalysisException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AI_PROVIDER_AUTH_FAILED);
    }

    @Test
    @DisplayName("AI 분석 실패 - 429 요청 한도 초과")
    void analyze_fail_rateLimitExceeded() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(429));
        Issue mockIssue = Issue.builder().message("Test error message").build();

        // when & then
        assertThatThrownBy(() -> openAIClient.analyze(mockIssue))
                .isInstanceOf(AIAnalysisException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AI_RATE_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("AI 분석 실패 - 500 서버 오류")
    void analyze_fail_serverError() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        Issue mockIssue = Issue.builder().message("Test error message").build();

        // when & then
        assertThatThrownBy(() -> openAIClient.analyze(mockIssue))
                .isInstanceOf(AIAnalysisException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AI_ANALYSIS_FAILED);
    }
}