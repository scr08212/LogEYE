package com.nkm.logeye.domain.auth;

import com.nkm.logeye.domain.auth.dto.LoginRequestDto;
import com.nkm.logeye.domain.auth.dto.TokenResponseDto;
import com.nkm.logeye.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public TokenResponseDto login(LoginRequestDto loginRequestDto){
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.email(), loginRequestDto.password());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        String accessToken = jwtProvider.createAccessToken(authentication.getName(), authentication.getAuthorities());
        String refreshToken = jwtProvider.createRefreshToken(authentication.getName());

        return new TokenResponseDto(accessToken, refreshToken);
    }
}