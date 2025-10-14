package com.nkm.logeye.domain.auth;

import com.nkm.logeye.domain.auth.dto.LoginRequestDto;
import com.nkm.logeye.domain.auth.dto.TokenResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto){
        TokenResponseDto tokenResponseDto = authService.login(loginRequestDto);

        return ResponseEntity.ok(tokenResponseDto);
    }
}
