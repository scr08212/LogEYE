package com.nkm.logeye.domain.account.dto;

import com.nkm.logeye.domain.account.Account;
import com.nkm.logeye.domain.account.AccountStatus;
import java.time.LocalDateTime;

public record AccountResponseDto(
        Long id,
        String email,
        String name,
        AccountStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
    public static AccountResponseDto from(Account account){
        return new AccountResponseDto(account.getId(), account.getEmail(), account.getName(), account.getStatus(), account.getCreatedAt(), account.getUpdatedAt());
    }
}