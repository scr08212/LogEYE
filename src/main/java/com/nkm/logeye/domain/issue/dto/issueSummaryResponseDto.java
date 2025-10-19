package com.nkm.logeye.domain.issue.dto;

import com.nkm.logeye.domain.issue.IssueLevel;
import com.nkm.logeye.domain.issue.IssueStatus;

import java.time.ZonedDateTime;

public record issueSummaryResponseDto(
        Long id,
        IssueLevel level,
        String message,
        IssueStatus status,
        Long eventCount,
        ZonedDateTime lastSeen
) {
}
