package com.nkm.logeye.domain.ingestion;

import com.nkm.logeye.domain.BaseTimeEntity;
import com.nkm.logeye.domain.project.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Issue extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    private String fingerprint;

    @Enumerated(EnumType.STRING)
    private IssueLevel level;

    private String message;

    private String stackTrace;

    @Enumerated(EnumType.STRING)
    private IssueStatus status;

    private Long eventCount;

    private ZonedDateTime lastSeen;

    @Builder
    public Issue(Project project, String fingerprint, IssueLevel level, String message, String stackTrace, IssueStatus status, Long eventCount, ZonedDateTime lastSeen) {
        this.project = project;
        this.fingerprint = fingerprint;
        this.level = level;
        this.message = message;
        this.stackTrace = stackTrace;
        this.status = status;
        this.eventCount = eventCount;
        this.lastSeen = lastSeen;
    }

    public void increaseEventCount() {
        this.eventCount++;
    }

    public void updateLastSeen(ZonedDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
}