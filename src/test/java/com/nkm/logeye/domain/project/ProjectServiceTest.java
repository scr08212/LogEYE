package com.nkm.logeye.domain.project;

import com.nkm.logeye.domain.account.Account;
import com.nkm.logeye.domain.account.AccountRepository;
import com.nkm.logeye.domain.account.AccountStatus;
import com.nkm.logeye.domain.project.dto.ProjectCreateRequestDto;
import com.nkm.logeye.global.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class ProjectServiceTest {

    @InjectMocks
    private ProjectService projectService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AccountRepository accountRepository;

    @Test
    @DisplayName("프로젝트 생성 성공")
    public void createProject_success(){
        // given
        ProjectCreateRequestDto requestDto = new ProjectCreateRequestDto("Project");
        String accountEmail = "test@gmail.com";
        Account fakeAccount = Account.builder()
                .email(accountEmail)
                .password("12345678")
                .name("김돌돌")
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findByEmail(accountEmail)).thenReturn(Optional.of(fakeAccount));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        Project savedProject = projectService.createProject(requestDto, accountEmail);
        // then
        assertThat(savedProject).isNotNull();
        assertThat(savedProject.getName()).isEqualTo("Project");
        assertThat(savedProject.getAccount()).isEqualTo(fakeAccount);
        assertThat(savedProject.getApiKey()).isNotBlank();

        verify(accountRepository).findByEmail(accountEmail);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 전체 조회 성공")
    void findProjectsByEmail_success() {
        // given
        String accountEmail = "test@example.com";
        Account fakeAccount = Account.builder().build();
        ReflectionTestUtils.setField(fakeAccount, "id", 1L);

        Project projectA = Project.builder().name("Project A").account(fakeAccount).build();
        Project projectB = Project.builder().name("Project B").account(fakeAccount).build();
        ReflectionTestUtils.setField(projectA, "id", 10L);
        ReflectionTestUtils.setField(projectB, "id", 11L);

        List<Project> fakeProjects = List.of(projectA, projectB);

        when(accountRepository.findByEmail(accountEmail)).thenReturn(Optional.of(fakeAccount));
        when(projectRepository.findAllByAccount(fakeAccount)).thenReturn(fakeProjects);

        // when
        List<Project> resultProjects = projectService.findAllByEmail(accountEmail);

        // then
        assertThat(resultProjects).isNotNull();
        assertThat(resultProjects.size()).isEqualTo(2);
        assertThat(resultProjects.get(0).getId()).isEqualTo(10L);
        assertThat(resultProjects.get(1).getId()).isEqualTo(11L);

        verify(accountRepository).findByEmail(accountEmail);
        verify(projectRepository).findAllByAccount(fakeAccount);
    }

    @Test
    @DisplayName("특정 프로젝트 조회 성공")
    void findProjectById_success(){
        // given
        Long projectId = 1L;
        String accountEmail = "test@example.com";
        Account fakeAccount = Account.builder().email(accountEmail).build();
        ReflectionTestUtils.setField(fakeAccount, "id", 1L);

        Project project = Project.builder().name("Project A").account(fakeAccount).build();
        ReflectionTestUtils.setField(project, "id", projectId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // when
        Project foundProject = projectService.findProjectById(projectId, accountEmail);

        // then
        assertThat(foundProject).isNotNull();
        assertThat(foundProject.getId()).isEqualTo(projectId);

        verify(projectRepository).findById(projectId);
    }

    @Test
    @DisplayName("특정 프로젝트 조회 실패 - 소유주가 아님")
    void findProjectById_fail(){
        // given
        Long projectId = 1L;
        String accountEmail = "test@example.com";

        Account ownerAccount = Account.builder().email("owner@gmail.com").build();
        ReflectionTestUtils.setField(ownerAccount, "id", 1L);

        Account fakeAccount = Account.builder().email(accountEmail).build();
        ReflectionTestUtils.setField(fakeAccount, "id", 10L);

        Project project = Project.builder().name("Project A").account(ownerAccount).build();
        ReflectionTestUtils.setField(project, "id", projectId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // when & then
        assertThatThrownBy(() ->  projectService.findProjectById(projectId, accountEmail))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("해당 프로젝트에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("특정 프로젝트 조회 실패 - 프로젝트가 없음")
    void findProjectById_fail_not_found(){
        // given
        Long projectId = 1L;
        String accountEmail = "test@example.com";
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->  projectService.findProjectById(projectId, accountEmail))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("특정 프로젝트 삭제 성공")
    void deleteProject_success(){
        // given
        Long projectId = 1L;
        String accountEmail = "test@example.com";
        Account fakeAccount = Account.builder().email(accountEmail).build();
        ReflectionTestUtils.setField(fakeAccount, "id", 1L);

        Project project = Project.builder().name("Project A").account(fakeAccount).build();
        ReflectionTestUtils.setField(project, "id", projectId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // when
        projectService.deleteProjectById(projectId, accountEmail);

        // then
        verify(projectRepository).deleteById(projectId);
    }

    @Test
    @DisplayName("특정 프로젝트 삭제 실패 - 소유주가 아님")
    void deleteProject_fail(){
        // given
        Long projectId = 1L;
        String accountEmail = "test@example.com";

        Account ownerAccount = Account.builder().email("owner@gmail.com").build();
        ReflectionTestUtils.setField(ownerAccount, "id", 1L);

        Account fakeAccount = Account.builder().email(accountEmail).build();
        ReflectionTestUtils.setField(fakeAccount, "id", 10L);

        Project project = Project.builder().name("Project A").account(ownerAccount).build();
        ReflectionTestUtils.setField(project, "id", projectId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // when & then
        assertThatThrownBy(() ->  projectService.deleteProjectById(projectId, accountEmail))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("해당 프로젝트에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("특정 프로젝트 삭제 실패 - 존재하지 않는 프로젝트")
    void deleteProject_fail_not_found() {
        // given
        Long projectId = 1L;
        String accountEmail = "test@example.com";
        Account fakeAccount = Account.builder().email(accountEmail).build();
        ReflectionTestUtils.setField(fakeAccount, "id", 10L);

        when(projectRepository.findById(anyLong())).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() ->  projectService.deleteProjectById(projectId, accountEmail))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}