package com.nkm.logeye.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequestDto(
        @NotBlank(message = "Refresh Token은 비어 있을 수 없습니다.")
        String refreshToken
) {
}
