package com.nkm.logeye.domain.project;

import com.nkm.logeye.domain.project.dto.ProjectCreateRequestDto;
import com.nkm.logeye.domain.project.dto.ProjectListResponseDto;
import com.nkm.logeye.domain.project.dto.ProjectResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(
            @RequestBody @Valid ProjectCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails){
        Project savedProject = projectService.createProject(requestDto, userDetails.getUsername());
        ProjectResponseDto responseDto = ProjectResponseDto.from(savedProject);

        URI location = URI.create("/api/v1/projects/" + savedProject.getId());
        return ResponseEntity.created(location).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<ProjectListResponseDto> getAllProject(@AuthenticationPrincipal UserDetails userDetails){
        List<Project> foundProjects = projectService.findAllByEmail(userDetails.getUsername());
        List<ProjectResponseDto> projectDtos = foundProjects.stream()
                .map(ProjectResponseDto::from)
                .toList();

        return ResponseEntity.ok(ProjectListResponseDto.from(projectDtos));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> getProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails){
        Project foundProject = projectService.findProjectById(projectId, userDetails.getUsername());

        ProjectResponseDto responseDto = ProjectResponseDto.from(foundProject);
        return ResponseEntity.ok().body(responseDto);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        projectService.deleteProjectById(projectId, userDetails.getUsername());

        return ResponseEntity.noContent().build();
    }
}