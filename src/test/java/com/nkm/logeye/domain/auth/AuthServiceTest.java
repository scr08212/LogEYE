package com.nkm.logeye.domain.auth;

import com.nkm.logeye.domain.auth.dto.LoginRequestDto;
import com.nkm.logeye.domain.auth.dto.TokenResponseDto;
import com.nkm.logeye.global.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("로그인 성공")
    void login_success(){
        // given
        LoginRequestDto requestDto = new LoginRequestDto("user@gmail.com", "12345678");
        String expectedAccessToken = "access-token";
        String expectedRefreshToken = "refresh-token";

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getName()).thenReturn(requestDto.email());

        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(mockAuthentication).getAuthorities();

        when(authenticationManager.authenticate(any())).thenReturn(mockAuthentication);
        when(jwtProvider.createAccessToken(requestDto.email(), authorities)).thenReturn(expectedAccessToken); // 여기도 변수 사용
        when(jwtProvider.createRefreshToken(requestDto.email())).thenReturn(expectedRefreshToken);

        // when
        TokenResponseDto tokenResponse = authService.login(requestDto);

        // then
        assertThat(tokenResponse.accessToken()).isEqualTo(expectedAccessToken);
        assertThat(tokenResponse.refreshToken()).isEqualTo(expectedRefreshToken);

        verify(authenticationManager).authenticate(any());

        verify(jwtProvider).createAccessToken(requestDto.email(), authorities);
        verify(jwtProvider).createRefreshToken(requestDto.email());
    }

    @Test
    @DisplayName("로그인 실패 - 자격 증명 실패")
    void login_fail_bad_credentials(){
        // given
        LoginRequestDto requestDto = new LoginRequestDto("user@gmail.com", "12345678");
        when(authenticationManager.authenticate(any())).thenThrow(BadCredentialsException.class);
        // when & then
        assertThatThrownBy(() -> authService.login(requestDto))
                .isInstanceOf(BadCredentialsException.class);
    }
}