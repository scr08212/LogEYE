package com.nkm.logeye.domain.project;

import com.nkm.logeye.domain.project.dto.ProjectCreateRequestDto;
import com.nkm.logeye.domain.project.dto.ProjectListResponseDto;
import com.nkm.logeye.domain.project.dto.ProjectResponseDto;
import com.nkm.logeye.global.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<ProjectResponseDto>> createProject(
            @RequestBody @Valid ProjectCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails){
        ProjectResponseDto responseDto = projectService.createProject(requestDto, userDetails.getUsername());

        URI location = URI.create("/api/v1/projects/" + responseDto.id());
        return ResponseEntity.created(location).body(ApiResponse.success(responseDto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProjectListResponseDto>> getAllProject(@AuthenticationPrincipal UserDetails userDetails){
        List<ProjectResponseDto> projectDtos = projectService.findAllByEmail(userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success(ProjectListResponseDto.from(projectDtos)));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> getProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails){
        ProjectResponseDto responseDto = projectService.findProjectById(projectId, userDetails.getUsername());
        return ResponseEntity.ok().body(ApiResponse.success(responseDto));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        projectService.deleteProjectById(projectId, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}