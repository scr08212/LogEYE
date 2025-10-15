package com.nkm.logeye.domain.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequestDto(
        @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        @Size(max = 255, message = "이메일 길이는 최대 255자입니다.")
        String email,

        @NotBlank(message = "비밀번호는 비어 있을 수 없습니다.")
        @Size(min = 8, max = 255, message = "비밀번호는 8자 이상, 255자 이하이어야 합니다.")
        String password,

        @NotBlank(message = "이름은 비어 있을 수 없습니다.")
        @Size(max = 100, message = "이름 길이는 최대 100자입니다.")
        String name
){
}