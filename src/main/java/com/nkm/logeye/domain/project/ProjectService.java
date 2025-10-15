package com.nkm.logeye.domain.project;

import com.nkm.logeye.domain.account.Account;
import com.nkm.logeye.domain.account.AccountRepository;
import com.nkm.logeye.domain.project.dto.ProjectCreateRequestDto;
import com.nkm.logeye.domain.project.dto.ProjectResponseDto;
import com.nkm.logeye.global.exception.ErrorCode;
import com.nkm.logeye.global.exception.ResourceNotFoundException;
import com.nkm.logeye.global.util.ApiKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public ProjectResponseDto createProject(ProjectCreateRequestDto requestDto, String accountEmail) {
        Account foundAccount = findAccountByEmail(accountEmail);
        String apiKey = ApiKeyGenerator.generateApiKey();

        Project project = Project.builder()
                .account(foundAccount)
                .name(requestDto.name())
                .apiKey(apiKey)
                .status(ProjectStatus.ACTIVE)
                .build();
        Project savedProject = projectRepository.save(project);

        return ProjectResponseDto.from(savedProject);
    }

    public List<ProjectResponseDto> findAllByEmail(String accountEmail){
        Account foundAccount = findAccountByEmail(accountEmail);
        List<Project> foundProjects = projectRepository.findAllByAccount(foundAccount);
        return foundProjects.stream()
                .map(ProjectResponseDto::from)
                .toList();
    }

    public ProjectResponseDto findProjectById(Long projectId, String accountEmail) {
        Project foundProject = findProjectByIdAndVerifyOwner(projectId, accountEmail);
        return ProjectResponseDto.from(foundProject);
    }

    @Transactional
    public void deleteProjectById(Long projectId, String accountEmail) {
        findProjectByIdAndVerifyOwner(projectId, accountEmail);

        projectRepository.deleteById(projectId);
    }

    private Project findProjectByIdAndVerifyOwner(Long projectId, String accountEmail){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.getAccount().getEmail().equals(accountEmail)) {
            throw new AccessDeniedException("해당 프로젝트에 접근할 권한이 없습니다.");
        }
        return project;
    }

    private Account findAccountByEmail(String accountEmail) {
        return accountRepository.findByEmail(accountEmail)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
}