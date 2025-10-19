package com.nkm.logeye.domain.issue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueEventRepository extends JpaRepository<IssueEvent, Long> {
    Page<IssueEvent> findByIssueId(Long issueId, Pageable pageable);
}