package com.nkm.logeye.domain.ingestion;

import com.nkm.logeye.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueEvent extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private Issue issue;

    @Column(columnDefinition = "jsonb")
    private String contextData;

    private ZonedDateTime occurredAt;

    @Builder
    public IssueEvent(Issue issue, String contextData, ZonedDateTime occurredAt) {
        this.issue = issue;
        this.contextData = contextData;
        this.occurredAt = occurredAt;
    }
}