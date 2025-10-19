package com.nkm.logeye.domain.issue;

import org.springframework.data.jpa.domain.Specification;

public class IssueSpecification {

    public static Specification<Issue> equalStatus(IssueStatus status) {
        // status가 null일 경우 필터링 조건을 적용하지 않기 위해 null이 아닌 Specification.where(null)을 반환할 수 있지만,
        // 서비스 레이어에서 null 체크를 하는 것이 더 명확합니다.
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<Issue> equalProjectId(Long projectId) {
        return (root, query, builder) -> builder.equal(root.get("project").get("id"), projectId);
    }
}