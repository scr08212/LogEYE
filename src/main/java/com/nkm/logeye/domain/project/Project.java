package com.nkm.logeye.domain.project;

import com.nkm.logeye.domain.BaseTimeEntity;
import com.nkm.logeye.domain.account.Account;
import com.nkm.logeye.domain.issue.Issue;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private String name;

    private String apiKey;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<Issue> issues;

    @Builder
    public Project(Account account, String name, String apiKey, ProjectStatus status){
        this.account = account;
        this.name = name;
        this.apiKey = apiKey;
        this.status = status;
    }
}
