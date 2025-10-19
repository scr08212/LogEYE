package com.nkm.logeye.domain.issue;

import org.springframework.data.jpa.domain.Specification;

public class IssueSpecification {

    public static Specification<Issue> and(Specification<Issue> spec, Specification<Issue> other) {
        return spec == null ? other : spec.and(other);
    }

    public static Specification<Issue> equalStatus(IssueStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<Issue> equalProjectId(Long projectId) {
        return (root, query, builder) -> builder.equal(root.get("project").get("id"), projectId);
    }
}