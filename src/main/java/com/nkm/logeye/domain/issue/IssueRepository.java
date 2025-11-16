package com.nkm.logeye.domain.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IssueRepository extends JpaRepository<Issue, Long>, JpaSpecificationExecutor<Issue> {
    Optional<Issue> findByProjectIdAndFingerprint(Long projectId, String fingerprint);

    @Query("""
            SELECT i
            FROM Issue i
            JOIN FETCH i.project p
            JOIN FETCH p.account a
            WHERE i.id = :issueId AND a.email = :accountEmail
            """)
    Optional<Issue> findByIdAndAccountEmail(@Param("issueId") Long issueId, @Param("accountEmail") String accountEmail);
}