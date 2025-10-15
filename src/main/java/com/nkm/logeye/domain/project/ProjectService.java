package com.nkm.logeye.domain.project;

import com.nkm.logeye.domain.account.Account;
import com.nkm.logeye.domain.account.AccountRepository;
import com.nkm.logeye.domain.project.dto.ProjectCreateRequestDto;
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
    public Project createProject(ProjectCreateRequestDto requestDto, String accountEmail) {
        Account foundAccount = findAccountByEmail(accountEmail);
        String apiKey = ApiKeyGenerator.generateApiKey();

        Project project = Project.builder()
                .account(foundAccount)
                .name(requestDto.name())
                .apiKey(apiKey)
                .status(ProjectStatus.ACTIVE)
                .build();
        return projectRepository.save(project);
    }

    public List<Project> findAllByEmail(String accountEmail){
        Account foundAccount = findAccountByEmail(accountEmail);
        return projectRepository.findAllByAccount(foundAccount);
    }

    public Project findProjectById(Long projectId, String accountEmail) {
        return findProjectByIdAndVerifyOwner(projectId, accountEmail);
    }

    @Transactional
    public void deleteProjectById(Long projectId, String accountEmail) {
        findProjectByIdAndVerifyOwner(projectId, accountEmail);

        projectRepository.deleteById(projectId);
    }

    private Project findProjectByIdAndVerifyOwner(Long projectId, String accountEmail){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (!project.getAccount().getEmail().equals(accountEmail)) {
            throw new AccessDeniedException("해당 프로젝트에 접근할 권한이 없습니다.");
        }
        return project;
    }

    private Account findAccountByEmail(String accountEmail) {
        return accountRepository.findByEmail(accountEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "email", accountEmail));
    }
}