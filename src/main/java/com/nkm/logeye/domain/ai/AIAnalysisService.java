package com.nkm.logeye.domain.ai;

import com.nkm.logeye.domain.ai.dto.AIAnalysisResponseDto;
import com.nkm.logeye.domain.ai.dto.AnalysisResultDto;
import com.nkm.logeye.domain.issue.Issue;
import com.nkm.logeye.domain.issue.IssueEvent;
import com.nkm.logeye.domain.issue.IssueEventRepository;
import com.nkm.logeye.domain.issue.IssueRepository;
import com.nkm.logeye.global.exception.ErrorCode;
import com.nkm.logeye.global.exception.ResourceNotFoundException;
import com.nkm.logeye.global.util.PromptGenerator;
import com.nkm.logeye.global.util.StackTraceParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIAnalysisService {

    private final AIAnalysisClient aiAnalysisClient;
    private final IssueRepository issueRepository;
    private final IssueEventRepository issueEventRepository;
    private final PromptGenerator promptGenerator;
    private final StackTraceParser stackTraceParser;

    @Transactional(readOnly = true)
    public AnalysisResultDto analysisIssue(Long issueId, String accountEmail){
        log.info("Starting AI analysis for Issue ID: {}", issueId);

        Issue issue = issueRepository.findByIdAndAccountEmail(issueId, accountEmail)
                .orElseThrow(() -> new AccessDeniedException("해당 이슈를 찾을 수 없거나 접근 권한이 없습니다."));
        IssueEvent issueEvent = issueEventRepository.findFirstByIssueIdOrderByOccurredAtDesc(issueId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        String prompt = promptGenerator.generate(issue, issueEvent);
        AIAnalysisResponseDto response = aiAnalysisClient.analyze(prompt);
        String parse = stackTraceParser.parse(issue.getStackTrace());

        log.info("Successfully completed AI analysis for Issue ID: {}", issueId);
        return new AnalysisResultDto(response.estimatedCause(), response.solutionSuggestion(), parse);
    }
}