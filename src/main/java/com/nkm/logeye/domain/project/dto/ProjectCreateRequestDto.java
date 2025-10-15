package com.nkm.logeye.domain.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectCreateRequestDto(
        @NotBlank(message = "프로젝트 명은 비어 있을 수 없습니다.")
        @Size(max = 100, message = "프로젝트 명은 최대 100자입니다.")
        String name
) {
}