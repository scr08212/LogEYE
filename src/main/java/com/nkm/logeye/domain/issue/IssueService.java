package com.nkm.logeye.domain.issue;

import com.nkm.logeye.domain.issue.dto.IssueDetailResponseDto;
import com.nkm.logeye.domain.issue.dto.IssueEventResponseDto;
import com.nkm.logeye.domain.issue.dto.IssueStatusUpdateRequestDto;
import com.nkm.logeye.domain.issue.dto.IssueSummaryResponseDto;
import com.nkm.logeye.domain.project.Project;
import com.nkm.logeye.domain.project.ProjectRepository;
import com.nkm.logeye.global.exception.ErrorCode;
import com.nkm.logeye.global.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;

    public Page<IssueSummaryResponseDto> findIssuesByProjectId(Long projectId, String accountEmail, Pageable pageable) {
        Project project = findProjectAndVerifyOwner(projectId, accountEmail);

        Page<Issue> issues = issueRepository.findByProjectId(projectId, pageable);

        return issues.map(issue -> new IssueSummaryResponseDto(
                issue.getId(),
                issue.getLevel(),
                issue.getMessage(),
                issue.getStatus(),
                issue.getEventCount(),
                issue.getLastSeen()
        ));
    }

    public IssueDetailResponseDto findIssueById(Long issueId, String accountEmail) {
        Issue issue = findIssueAndVerifyOwner(issueId, accountEmail);

        List<IssueEventResponseDto> eventDtos = issue.getIssueEvents().stream()
                .map(event -> new IssueEventResponseDto(event.getId(), event.getOccurredAt(), event.getContextData()))
                .toList();

        return new IssueDetailResponseDto(
                issue.getId(),
                issue.getLevel(),
                issue.getMessage(),
                issue.getStackTrace(),
                issue.getStatus(),
                issue.getEventCount(),
                issue.getLastSeen(),
                eventDtos
        );
    }

    @Transactional
    public void updateIssueStatus(Long issueId, String accountEmail, IssueStatusUpdateRequestDto requestDto) {
        Issue issue = findIssueAndVerifyOwner(issueId, accountEmail);

        issue.updateStatus(requestDto.status());
    }


    private Project findProjectAndVerifyOwner(Long projectId, String accountEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.getAccount().getEmail().equals(accountEmail)) {
            throw new AccessDeniedException("해당 프로젝트에 접근할 권한이 없습니다.");
        }
        return project;
    }

    private Issue findIssueAndVerifyOwner(Long issueId, String accountEmail) {
        Issue issue = issueRepository.findByIdWithEvents(issueId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ISSUE_NOT_FOUND)); // ErrorCode에 추가 필요

        if (!issue.getProject().getAccount().getEmail().equals(accountEmail)) {
            throw new AccessDeniedException("해당 이슈에 접근할 권한이 없습니다.");
        }
        return issue;
    }
}