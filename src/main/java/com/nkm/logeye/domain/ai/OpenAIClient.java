package com.nkm.logeye.domain.ai;

import com.nkm.logeye.domain.ai.dto.AIAnalysisResponseDto;
import com.nkm.logeye.global.exception.AIAnalysisException;
import com.nkm.logeye.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;


@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAIClient implements AIAnalysisClient{
    private final WebClient.Builder webClientBuilder;
    private final AIProperties aiProperties;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    @Override
    public AIAnalysisResponseDto analyze(String prompt) {
        WebClient webClient = webClientBuilder
                .baseUrl(aiProperties.getUrl())
                .defaultHeader("Authorization", "Bearer " + aiProperties.getApiKey())
                .build();

        // 프롬프트 생성
        Object requestBody = createRequestBody(prompt);
        log.info("Requesting AI analysis...");

        // API 호출 및 결과 파싱
        try{
            return webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("AI API Error Response: Status = {}, Body = {}", response.statusCode(), response.bodyToMono(String.class));
                        if(response.statusCode().is4xxClientError())
                        {
                            if(response.statusCode() == HttpStatus.UNAUTHORIZED ||  response.statusCode() == HttpStatus.FORBIDDEN){
                                return Mono.error(new AIAnalysisException(ErrorCode.AI_PROVIDER_AUTH_FAILED));
                            }
                            if(response.statusCode() == HttpStatus.TOO_MANY_REQUESTS){
                                return Mono.error(new AIAnalysisException(ErrorCode.AI_RATE_LIMIT_EXCEEDED));
                            }
                        }
                        return Mono.error(new AIAnalysisException(ErrorCode.AI_ANALYSIS_FAILED));
                    })
                    .bodyToMono(AIAnalysisResponseDto.class)
                    .timeout(REQUEST_TIMEOUT)
                    .doOnError(throwable -> log.error("Error during AI analysis request.", throwable))
                    .onErrorMap(e->!(e instanceof AIAnalysisException), e -> new AIAnalysisException(ErrorCode.AI_ANALYSIS_FAILED, e))
                    .block();
        } catch (AIAnalysisException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get AI analysis. Error: {}", e.getMessage());
            throw new AIAnalysisException(ErrorCode.AI_ANALYSIS_FAILED, e);
        }
    }

    private Object createRequestBody(String prompt) {
        return new OpenAIRequest(
                "gpt-4o",
                prompt,
                0.7,
                1024,
                false
        );
    }

    private record OpenAIRequest(String model, String prompt, double temperature, int maxTokens, boolean stream){}
}
