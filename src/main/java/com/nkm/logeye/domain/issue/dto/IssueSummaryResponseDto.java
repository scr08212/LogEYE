package com.nkm.logeye.domain.issue.dto;

import com.nkm.logeye.domain.issue.Issue;
import com.nkm.logeye.domain.issue.IssueLevel;
import com.nkm.logeye.domain.issue.IssueStatus;

import java.time.ZonedDateTime;

public record IssueSummaryResponseDto(
        Long id,
        IssueLevel level,
        String message,
        IssueStatus status,
        Long eventCount,
        ZonedDateTime lastSeen
) {
    public static IssueSummaryResponseDto from(Issue issue) {
        return new IssueSummaryResponseDto(
                issue.getId(), issue.getLevel(), issue.getMessage(),
                issue.getStatus(), issue.getEventCount(), issue.getLastSeen()
        );
    }
}
