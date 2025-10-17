package com.nkm.logeye.domain.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public record IssueEventRequestDto(
        @NotBlank(message = "에러 메세지는 비어 있을 수 없습니다.")
        String message,
        String stackTrace,
        @NotNull(message = "발생 시각은 비어 있을 수 없습니다.")
        ZonedDateTime occurredAt,

        // Json context
        String environment,
        String appVersion,
        String contextData
) {

}
