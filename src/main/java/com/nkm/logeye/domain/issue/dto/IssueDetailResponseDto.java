package com.nkm.logeye.domain.issue.dto;

import com.nkm.logeye.domain.issue.Issue;
import com.nkm.logeye.domain.issue.IssueLevel;
import com.nkm.logeye.domain.issue.IssueStatus;

import java.time.ZonedDateTime;
import java.util.List;

public record IssueDetailResponseDto (
        Long id,
        IssueLevel level,
        String message,
        String stackTrace,
        IssueStatus status,
        Long eventCount,
        ZonedDateTime lastSeen,
        List<IssueEventResponseDto> events
) {
    public static IssueDetailResponseDto from(Issue issue) {
        List<IssueEventResponseDto> eventDtos = issue.getIssueEvents().stream()
                .map(IssueEventResponseDto::from)
                .toList();

        return new IssueDetailResponseDto(
                issue.getId(), issue.getLevel(), issue.getMessage(), issue.getStackTrace(),
                issue.getStatus(), issue.getEventCount(), issue.getLastSeen(), eventDtos
        );
    }
}