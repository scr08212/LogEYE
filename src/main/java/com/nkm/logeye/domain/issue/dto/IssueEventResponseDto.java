package com.nkm.logeye.domain.issue.dto;

import com.nkm.logeye.domain.issue.IssueEvent;

import java.time.ZonedDateTime;

public record IssueEventResponseDto(
        Long id,
        ZonedDateTime occurredAt,
        String contextData
) {
    public static IssueEventResponseDto from(IssueEvent issueEvent) {
        return new IssueEventResponseDto(
                issueEvent.getId(),
                issueEvent.getOccurredAt(),
                issueEvent.getContextData()
        );
    }
}