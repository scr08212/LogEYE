package com.nkm.logeye.domain.project.dto;

import java.util.List;

public record ProjectListResponseDto(
        List<ProjectResponseDto> projects
) {
    public static ProjectListResponseDto from(List<ProjectResponseDto> projects){
        return new  ProjectListResponseDto(projects);
    }
}
