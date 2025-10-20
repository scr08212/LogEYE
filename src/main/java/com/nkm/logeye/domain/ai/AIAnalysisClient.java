package com.nkm.logeye.domain.ai;

import com.nkm.logeye.domain.issue.Issue;

public interface AIAnalysisClient {
    AnalysisResult analyze(Issue issue);
}
