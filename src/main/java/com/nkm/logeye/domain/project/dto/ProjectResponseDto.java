package com.nkm.logeye.domain.project.dto;

import com.nkm.logeye.domain.project.Project;
import com.nkm.logeye.domain.project.ProjectStatus;

import java.time.ZonedDateTime;

public record ProjectResponseDto(
        Long id,
        Long accountId,
        String name,
        String apiKey,
        ProjectStatus status,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static ProjectResponseDto from(Project project) {
        return new ProjectResponseDto(
                project.getId(),
                project.getAccount().getId(),
                project.getName(),
                project.getApiKey(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}