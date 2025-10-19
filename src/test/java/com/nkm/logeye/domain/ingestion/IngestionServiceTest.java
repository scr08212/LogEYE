package com.nkm.logeye.domain.ingestion;

import com.nkm.logeye.domain.issue.Issue;
import com.nkm.logeye.domain.issue.IssueEvent;
import com.nkm.logeye.domain.issue.IssueEventRepository;
import com.nkm.logeye.domain.issue.IssueRepository;
import com.nkm.logeye.domain.issue.dto.IssueEventRequestDto;
import com.nkm.logeye.domain.project.Project;
import com.nkm.logeye.domain.project.ProjectRepository;
import com.nkm.logeye.global.exception.BusinessException;
import com.nkm.logeye.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {
    @InjectMocks
    private IngestionService ingestionService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private IssueEventRepository issueEventRepository;

    @Test
    @DisplayName("신규 에러 수신 - Issue와 IssueEvent 생성 성공")
    void processIssueEvent_success_newIssue(){
        // given
        String apiKey = "apiKey";
        IssueEventRequestDto requestDto = new IssueEventRequestDto(
                "New NullPointerException",
                "at com.example.NewService.call(NewService.java:10)",
                ZonedDateTime.now(), "production", "1.0", "{}");

        Project fakeProject = Project.builder().build();
        ReflectionTestUtils.setField(fakeProject, "id", 1L);

        // 1. API Key 검증이 통과하도록 설정
        when(projectRepository.findByApiKey(apiKey)).thenReturn(Optional.of(fakeProject));

        // 2. "신규 에러" 상황을 시뮬레이션: findBy... 결과가 비어있도록 설정
        when(issueRepository.findByProjectIdAndFingerprint(anyLong(), anyString())).thenReturn(Optional.empty());

        // 3. save 메소드가 호출될 때, 저장되는 객체를 나중에 검증하기 위해 ArgumentCaptor 준비
        ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
        ArgumentCaptor<IssueEvent> issueEventCaptor = ArgumentCaptor.forClass(IssueEvent.class);

        // when
        ingestionService.processIssueEvent(apiKey, requestDto);

        // then
        // 4. issueRepository의 save가 정확히 1번 호출되었는지 검증하고, 전달된 Issue 객체를 캡처
        verify(issueRepository, times(1)).save(issueCaptor.capture());

        // 5. issueEventRepository의 save가 정확히 1번 호출되었는지 검증하고, 전달된 IssueEvent 객체를 캡처
        verify(issueEventRepository, times(1)).save(issueEventCaptor.capture());

        // 6. 캡처된 객체들의 내부 값을 검증
        Issue capturedIssue = issueCaptor.getValue();
        assertThat(capturedIssue.getEventCount()).isEqualTo(1L);
        assertThat(capturedIssue.getMessage()).isEqualTo(requestDto.message());

        IssueEvent capturedEvent = issueEventCaptor.getValue();
        assertThat(capturedEvent.getOccurredAt()).isEqualTo(requestDto.occurredAt());
    }

    @Test
    @DisplayName("중복 에러 수신 - Issue는 업데이트 IssueEvent만 생성 성공")
    void processIssueEvent_success_duplicateIssue(){
        // given
        String apiKey = "apiKey";
        IssueEventRequestDto requestDto = new IssueEventRequestDto(
                "New NullPointerException",
                "at com.example.NewService.call(NewService.java:10)",
                ZonedDateTime.now(), "production", "1.0", "{}");

        Project fakeProject = Project.builder().build();
        ReflectionTestUtils.setField(fakeProject, "id", 1L);

        Issue existingIssue = Issue.builder()
                .eventCount(5L)
                .build();

        // Mockito의 Spy를 사용해 실제 객체의 메소드 호출을 추적
        Issue spiedIssue = spy(existingIssue);

        when(projectRepository.findByApiKey(apiKey)).thenReturn(Optional.of(fakeProject));
        // findBy...가 spiedIssue를 반환하도록 설정
        when(issueRepository.findByProjectIdAndFingerprint(anyLong(), anyString())).thenReturn(Optional.of(spiedIssue));

        // when
        ingestionService.processIssueEvent(apiKey, requestDto);

        // then
        // 2. spiedIssue의 increaseEventCount와 updateLastSeen 메소드가 호출되었는지 검증
        verify(spiedIssue, times(1)).increaseEventCount();
        verify(spiedIssue, times(1)).updateLastSeen(requestDto.occurredAt());

        // 3. issueRepository의 save는 "업데이트"를 위해 1번 호출되었는지 검증
        verify(issueRepository, times(1)).save(spiedIssue);

        // 4. issueEventRepository의 save는 "신규 생성"을 위해 1번 호출되었는지 검증
        verify(issueEventRepository, times(1)).save(any(IssueEvent.class));
        // then
    }

    @Test
    @DisplayName("실패 - 유효하지 않은 API Key")
    void processIssueEvent_fail_invalidApiKey(){
        // given
        String invalidApiKey = "invalid-api-key";
        IssueEventRequestDto requestDto = new IssueEventRequestDto(
                "New NullPointerException",
                "at com.example.NewService.call(NewService.java:10)",
                ZonedDateTime.now(), "production", "1.0", "{}");

        // 1. API Key를 찾지 못하는 상황 시뮬레이션
        when(projectRepository.findByApiKey(invalidApiKey)).thenReturn(Optional.empty());

        // when & then
        // 2. BusinessException이 발생하는지, ErrorCode가 올바른지 검증
        assertThatThrownBy(() -> ingestionService.processIssueEvent(invalidApiKey, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_API_KEY);
    }
}