package com.nkm.logeye.domain.account.dto;

import com.nkm.logeye.domain.account.Account;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignupRequestDto(
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

    public static SignupRequestDto from(Account account){
        return new SignupRequestDto(account.getEmail(), account.getPassword(), account.getName());
    }
}