package com.nkm.logeye.domain.issue;

import com.nkm.logeye.domain.issue.dto.IssueDetailResponseDto;
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
@RequestMapping("/api/v1/projects/{projectId}")
public class IssueController {

    private final IssueService issueService;

    @GetMapping("/issues")
    public ResponseEntity<ApiResponse<Page<IssueSummaryResponseDto>>> getIssuesByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) IssueStatus status,
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        String accountEmail = userDetails.getUsername();
        Page<IssueSummaryResponseDto> issuePage = issueService.findIssuesByProjectId(projectId, accountEmail, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(issuePage));
    }

    @GetMapping("/issues/{issueId}")
    public ResponseEntity<ApiResponse<IssueDetailResponseDto>> getIssueDetails(
            @PathVariable Long issueId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String accountEmail = userDetails.getUsername();
        IssueDetailResponseDto issueDetail = issueService.findIssueById(issueId, accountEmail);
        return ResponseEntity.ok(ApiResponse.success(issueDetail));
    }

    @PatchMapping("/issues/{issueId}/status")
    public ResponseEntity<ApiResponse<Void>> updateIssueStatus(
            @PathVariable Long issueId,
            @RequestBody @Valid IssueStatusUpdateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String accountEmail = userDetails.getUsername();
        issueService.updateIssueStatus(issueId, accountEmail, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
