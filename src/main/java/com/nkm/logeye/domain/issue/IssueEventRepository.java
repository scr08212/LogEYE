package com.nkm.logeye.domain.issue;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueEventRepository extends JpaRepository<IssueEvent, Long> {
}