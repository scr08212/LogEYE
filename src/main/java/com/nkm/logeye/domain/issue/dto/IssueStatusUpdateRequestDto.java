package com.nkm.logeye.domain.issue.dto;

import com.nkm.logeye.domain.issue.IssueStatus;
import jakarta.validation.constraints.NotNull;

public record IssueStatusUpdateRequestDto(
        @NotNull(message = "변경할 상태 값은 비어있을 수 없습니다.")
        IssueStatus status
) {
}