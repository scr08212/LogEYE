package com.nkm.logeye.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-001", "Invalid Input Value"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON-002", "Access is Denied"),

    // Account
    EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST, "ACCOUNT-001", "Email is Duplicated"),

    // Auth
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH-001", "Authentication failed"),
    INVALID_API_KEY(HttpStatus.FORBIDDEN, "AUTH-002", "Invalid API Key"),

    // Resource Not Found
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-003", "Resource not found"),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-004", "Project not found"),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-005", "Account not found"),
    ISSUE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-006", "Issue not found"),

    // AI Analysis
    AI_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI-001", "AI analysis service failed"),
    AI_PROVIDER_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "AI-002", "AI provider authentication failed. Check your API key."),
    AI_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AI-003", "AI provider rate limit exceeded."),
    AI_INVALID_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "AI-004", "Invalid response from AI analysis service");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
