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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
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
        List<Issue> issueList = List.of(issue);
        Page<Issue> issuePage = new PageImpl<>(issueList, pageable, 1);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(issueRepository.findByProjectId(project.getId(), pageable)).thenReturn(issuePage);

        // when
        issueService.findIssuesByProjectId(project.getId(), owner.getEmail(), pageable);

        // then
        verify(projectRepository).findById(project.getId());
        verify(issueRepository).findByProjectId(project.getId(), pageable);
    }

    @Test
    @DisplayName("프로젝트별 이슈 목록 조회 실패 - 소유주가 아님")
    void findIssuesByProjectId_fail_notOwner() {
        // given
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        // when & then
        assertThatThrownBy(() -> issueService.findIssuesByProjectId(project.getId(), other.getEmail(), PageRequest.of(0, 10)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("해당 프로젝트에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("이슈 상세 조회 성공")
    void findIssueById_success() {
        // given
        when(issueRepository.findByIdWithEvents(issue.getId())).thenReturn(Optional.of(issue));

        // when
        issueService.findIssueById(issue.getId(), owner.getEmail());

        // then
        verify(issueRepository).findByIdWithEvents(issue.getId());
    }

    @Test
    @DisplayName("이슈 상세 조회 실패 - 존재하지 않는 이슈")
    void findIssueById_fail_notFound() {
        // given
        Long nonExistentIssueId = 999L;
        when(issueRepository.findByIdWithEvents(nonExistentIssueId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> issueService.findIssueById(nonExistentIssueId, owner.getEmail()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("이슈 상세 조회 실패 - 소유주가 아님")
    void findIssueById_fail_notOwner() {
        // given
        when(issueRepository.findByIdWithEvents(issue.getId())).thenReturn(Optional.of(issue));

        // when & then
        assertThatThrownBy(() -> issueService.findIssueById(issue.getId(), other.getEmail()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("해당 이슈에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("이슈 상태 변경 성공")
    void updateIssueStatus_success() {
        // given
        IssueStatusUpdateRequestDto requestDto = new IssueStatusUpdateRequestDto(IssueStatus.RESOLVED);
        Issue spiedIssue = spy(issue); // 실제 객체의 메소드 호출을 감시하기 위해 spy 사용

        when(issueRepository.findByIdWithEvents(issue.getId())).thenReturn(Optional.of(spiedIssue));

        // when
        issueService.updateIssueStatus(issue.getId(), owner.getEmail(), requestDto);

        // then
        verify(issueRepository).findByIdWithEvents(issue.getId());
        verify(spiedIssue).updateStatus(IssueStatus.RESOLVED); // updateStatus 메소드가 RESOLVED 값으로 호출되었는지 검증
        assertThat(spiedIssue.getStatus()).isEqualTo(IssueStatus.RESOLVED);
    }
}