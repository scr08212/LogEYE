package com.nkm.logeye.domain.ingestion;

import com.nkm.logeye.domain.ingestion.dto.IssueEventRequestDto;
import com.nkm.logeye.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/logs")
public class IngestionController {
    private final IngestionService ingestionService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> ingestLog(
            @RequestHeader("X-LOGEYE-API-KEY") String apiKey,
            @RequestBody @Valid IssueEventRequestDto requestDto
            ){

        ingestionService.processIssueEvent(apiKey, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }
}