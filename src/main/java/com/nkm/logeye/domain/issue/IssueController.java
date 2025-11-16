package com.nkm.logeye.domain.issue;

import com.nkm.logeye.domain.ai.AIAnalysisService;
import com.nkm.logeye.domain.ai.dto.AnalysisResultDto;
import com.nkm.logeye.domain.issue.dto.IssueDetailResponseDto;
import com.nkm.logeye.domain.issue.dto.IssueEventResponseDto;
import com.nkm.logeye.domain.issue.dto.IssueStatusUpdateRequestDto;
import com.nkm.logeye.domain.issue.dto.IssueSummaryResponseDto;
import com.nkm.logeye.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/issues")
public class IssueController {

    private final IssueService issueService;
    private final AIAnalysisService aiAnalysisService;

    @GetMapping()
    public ResponseEntity<ApiResponse<Page<IssueSummaryResponseDto>>> getIssuesByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) IssueStatus status,
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        String accountEmail = userDetails.getUsername();
        Page<IssueSummaryResponseDto> issuePage = issueService.findIssuesByProjectId(projectId, accountEmail, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(issuePage));
    }

    @GetMapping("/{issueId}")
    public ResponseEntity<ApiResponse<IssueDetailResponseDto>> getIssueDetails(
            @PathVariable Long issueId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String accountEmail = userDetails.getUsername();
        IssueDetailResponseDto issueDetail = issueService.findIssueById(issueId, accountEmail);
        return ResponseEntity.ok(ApiResponse.success(issueDetail));
    }

    @PatchMapping("/{issueId}/status")
    public ResponseEntity<ApiResponse<Void>> updateIssueStatus(
            @PathVariable Long issueId,
            @RequestBody @Valid IssueStatusUpdateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String accountEmail = userDetails.getUsername();
        issueService.updateIssueStatus(issueId, accountEmail, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{issueId}/events")
    public ResponseEntity<ApiResponse<Page<IssueEventResponseDto>>> getIssueEvents(
            @PathVariable Long issueId,
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        String accountEmail = userDetails.getUsername();
        Page<IssueEventResponseDto> eventPage = issueService.findEventsByIssueId(issueId, accountEmail, pageable);
        return ResponseEntity.ok(ApiResponse.success(eventPage));
    }

    @PostMapping("/{issueId}/analyze")
    public ResponseEntity<ApiResponse<AnalysisResultDto>> analyzeIssue(
            @PathVariable Long issueId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String accountEmail = userDetails.getUsername();
        AnalysisResultDto analysisResultDto = aiAnalysisService.analysisIssue(issueId, accountEmail);
        return ResponseEntity.ok(ApiResponse.success(analysisResultDto));
    }
}
