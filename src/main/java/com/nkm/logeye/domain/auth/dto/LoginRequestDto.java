package com.nkm.logeye.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @NotBlank(message = "이메일을 입력해주세요.")
        @Size(max = 255)
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(max = 255)
        String password
){

}