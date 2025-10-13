package com.nkm.logeye.domain.account.dto;

import com.nkm.logeye.domain.account.Account;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @NotNull
        @Size(max = 255)
        String email,

        @NotNull
        @Size(max = 255)
        String password,

        @NotNull
        @Size(max = 100)
        String name
){

    public static LoginRequestDto from(Account account){
        return new LoginRequestDto(account.getEmail(), account.getPassword(), account.getName());
    }
}