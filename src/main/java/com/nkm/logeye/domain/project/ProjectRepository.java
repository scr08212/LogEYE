package com.nkm.logeye.domain.project;

import com.nkm.logeye.domain.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByAccount(Account account);
}
