package com.nkm.logeye.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnalysisResultDto(
        @JsonProperty("estimated_cause")
        String estimatedCause,
        @JsonProperty("solution_suggestion")
        String solutionSuggestion,
        @JsonProperty("impacted_file")
        String impactedFile
) {
}