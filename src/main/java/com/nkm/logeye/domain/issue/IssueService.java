package com.nkm.logeye.domain.issue;

import com.nkm.logeye.domain.issue.dto.IssueDetailResponseDto;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository; // findIssuesByProjectId 에서는 여전히 필요

    public Page<IssueSummaryResponseDto> findIssuesByProjectId(Long projectId, String accountEmail, IssueStatus status, Pageable pageable) {
        Project project = findProjectAndVerifyOwner(projectId, accountEmail);

        Specification<Issue> spec = IssueSpecification.equalProjectId(project.getId());
        if (status != null) {
            spec = spec.and(IssueSpecification.equalStatus(status));
        }

        Page<Issue> issues = issueRepository.findAll(spec, pageable);

        return issues.map(IssueSummaryResponseDto::from);
    }

    public IssueDetailResponseDto findIssueById(Long issueId, String accountEmail) {
        Issue issue = issueRepository.findByIdAndAccountEmail(issueId, accountEmail)
                .orElseThrow(() -> new AccessDeniedException("해당 이슈를 찾을 수 없거나 접근 권한이 없습니다."));

        return IssueDetailResponseDto.from(issue);
    }

    @Transactional
    public void updateIssueStatus(Long issueId, String accountEmail, IssueStatusUpdateRequestDto requestDto) {
        Issue issue = issueRepository.findByIdAndAccountEmail(issueId, accountEmail)
                .orElseThrow(() -> new AccessDeniedException("해당 이슈를 찾을 수 없거나 접근 권한이 없습니다."));

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
}