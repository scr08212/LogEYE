package com.nkm.logeye.domain.issue;

import com.nkm.logeye.domain.account.Account;
import com.nkm.logeye.domain.issue.dto.IssueStatusUpdateRequestDto;
import com.nkm.logeye.domain.project.Project;
import com.nkm.logeye.domain.project.ProjectRepository;
import com.nkm.logeye.global.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @InjectMocks
    private IssueService issueService;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ProjectRepository projectRepository;

    private Account owner;
    private Account other;
    private Project project;
    private Issue issue;

    @BeforeEach
    void setUp() {
        owner = Account.builder().email("owner@test.com").name("owner").build();
        ReflectionTestUtils.setField(owner, "id", 1L);

        other = Account.builder().email("other@test.com").name("other").build();
        ReflectionTestUtils.setField(other, "id", 2L);

        project = Project.builder().account(owner).name("My Project").build();
        ReflectionTestUtils.setField(project, "id", 10L);

        issue = Issue.builder().project(project).message("Test Issue").build();
        ReflectionTestUtils.setField(issue, "id", 100L);
    }

    @Test
    @DisplayName("프로젝트별 이슈 목록 조회 성공")
    void findIssuesByProjectId_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(issueRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        // when
        issueService.findIssuesByProjectId(project.getId(), owner.getEmail(), null, pageable);

        // then
        verify(projectRepository).findById(project.getId());
        verify(issueRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("프로젝트별 이슈 목록 조회 실패 - 소유주가 아님")
    void findIssuesByProjectId_fail_notOwner() {
        // given
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        // when & then
        assertThatThrownBy(() -> issueService.findIssuesByProjectId(project.getId(), other.getEmail(), null, PageRequest.of(0, 10)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("해당 프로젝트에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("이슈 상세 조회 성공")
    void findIssueById_success() {
        // given
        when(issueRepository.findByIdAndAccountEmail(issue.getId(), owner.getEmail()))
                .thenReturn(Optional.of(issue));

        // when
        issueService.findIssueById(issue.getId(), owner.getEmail());

        // then
        verify(issueRepository).findByIdAndAccountEmail(issue.getId(), owner.getEmail());
    }

    @Test
    @DisplayName("이슈 상세 조회 실패 - 존재하지 않는 이슈 또는 권한 없음")
    void findIssueById_fail_notFoundOrNotOwner() {
        // given
        Long nonExistentIssueId = 999L;
        when(issueRepository.findByIdAndAccountEmail(nonExistentIssueId, owner.getEmail()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> issueService.findIssueById(nonExistentIssueId, owner.getEmail()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("해당 이슈를 찾을 수 없거나 접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("이슈 상세 조회 실패 - 소유주가 아님")
    void findIssueById_fail_notOwner() {
        // given
        when(issueRepository.findByIdAndAccountEmail(issue.getId(), other.getEmail()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> issueService.findIssueById(issue.getId(), other.getEmail()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("해당 이슈를 찾을 수 없거나 접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("이슈 상태 변경 성공")
    void updateIssueStatus_success() {
        // given
        IssueStatusUpdateRequestDto requestDto = new IssueStatusUpdateRequestDto(IssueStatus.RESOLVED);
        Issue spiedIssue = spy(issue);

        when(issueRepository.findByIdAndAccountEmail(issue.getId(), owner.getEmail()))
                .thenReturn(Optional.of(spiedIssue));

        // when
        issueService.updateIssueStatus(issue.getId(), owner.getEmail(), requestDto);

        // then
        verify(issueRepository).findByIdAndAccountEmail(issue.getId(), owner.getEmail());
        verify(spiedIssue).updateStatus(IssueStatus.RESOLVED);
        assertThat(spiedIssue.getStatus()).isEqualTo(IssueStatus.RESOLVED);
    }
}