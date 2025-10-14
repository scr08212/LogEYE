package com.nkm.logeye.domain.auth.dto;

public record TokenResponseDto(
        String accessToken,
        String refreshToken
) {
}
