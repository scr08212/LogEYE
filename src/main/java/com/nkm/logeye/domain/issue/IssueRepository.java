package com.nkm.logeye.domain.issue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IssueRepository extends JpaRepository<Issue, Long>, JpaSpecificationExecutor<Issue> {
    Optional<Issue> findByProjectIdAndFingerprint(Long projectId, String fingerprint);

    Page<Issue> findByProjectId(Long projectId, Pageable pageable);

    @Query("SELECT i FROM Issue i LEFT JOIN FETCH i.issueEvents WHERE i.id = :issueId")
    Optional<Issue> findByIdWithEvents(@Param("issueId") Long issueId);

    @Query("SELECT i FROM Issue i JOIN i.project p JOIN p.account a WHERE i.id = :issueId AND a.email = :accountEmail")
    Optional<Issue> findByIdAndAccountEmail(@Param("issueId") Long issueId, @Param("accountEmail") String accountEmail);
}