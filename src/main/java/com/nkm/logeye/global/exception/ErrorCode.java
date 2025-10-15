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

    // Resource Not Found
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-003", "Resource not found"),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-004", "Project not found"),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-005", "Account not found");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
