package com.nkm.logeye.domain.ingestion;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueEventRepository extends JpaRepository<IssueEvent, Long> {
}