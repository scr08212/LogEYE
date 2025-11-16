package com.nkm.logeye.domain.ai;

import com.nkm.logeye.domain.ai.dto.AIAnalysisResponseDto;

public interface AIAnalysisClient {
    AIAnalysisResponseDto analyze(String prompt);
}
