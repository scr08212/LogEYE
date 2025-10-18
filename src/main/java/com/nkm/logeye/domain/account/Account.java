package com.nkm.logeye.domain.account;

import com.nkm.logeye.domain.BaseTimeEntity;
import com.nkm.logeye.domain.project.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String name;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects;

    @Builder
    public Account(String email, String password, String name, AccountStatus status) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.status = status;
    }
}