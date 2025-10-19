package com.nkm.logeye.domain.ingestion;

import com.nkm.logeye.domain.issue.*;
import com.nkm.logeye.domain.issue.dto.IssueEventRequestDto;
import com.nkm.logeye.domain.project.Project;
import com.nkm.logeye.domain.project.ProjectRepository;
import com.nkm.logeye.global.exception.BusinessException;
import com.nkm.logeye.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final IssueEventRepository issueEventRepository;

    @Transactional
    public void processIssueEvent(String apiKey, IssueEventRequestDto requestDto) {
        // 1. apiKey 검증
        Project project = projectRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_API_KEY));

        // 2. fingerprint 생성
        String fingerprint = createFingerprint(requestDto);

        // 3. fingerprint 기반 Issue 조회 또는 생성
        Issue issue = issueRepository.findByProjectIdAndFingerprint(project.getId(), fingerprint)
                .map(existingissue -> {
                    existingissue.increaseEventCount();
                    existingissue.updateLastSeen(requestDto.occurredAt());
                    return existingissue;
                })
                .orElseGet(() -> Issue.builder()
                        .project(project)
                        .fingerprint(fingerprint)
                        .level(IssueLevel.ERROR)
                        .message(requestDto.message())
                        .stackTrace(requestDto.stackTrace())
                        .status(IssueStatus.UNHANDLED)
                        .eventCount(1L)
                        .lastSeen(requestDto.occurredAt())
                        .build());

        Issue savedIssue = issueRepository.save(issue);
        // 4. IssueEvent 생성 및 저장
        IssueEvent issueEvent = IssueEvent.builder()
                .issue(savedIssue)
                .occurredAt(requestDto.occurredAt())
                .contextData(requestDto.contextData())
                .build();

        issueEventRepository.save(issueEvent);
    }

    private String createFingerprint(IssueEventRequestDto requestDto) {
        // 임시!
        // 추후 더 정교하게 구현할 것
        String stackTraceLine = requestDto.stackTrace() != null && !requestDto.stackTrace().isBlank()
                ? requestDto.stackTrace().split("\n")[0]
                : "";

        String source = requestDto.message() + stackTraceLine;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(NoSuchAlgorithmException e){
            throw new RuntimeException("Could not create fingerprint", e);
        }
    }
}
