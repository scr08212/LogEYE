package com.nkm.logeye.domain.account;

import com.nkm.logeye.domain.account.dto.AccountResponseDto;
import com.nkm.logeye.domain.account.dto.SignupRequestDto;
import com.nkm.logeye.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponseDto>> signup(@RequestBody @Valid SignupRequestDto signupRequestDto) {
        AccountResponseDto accountResponseDto = accountService.signup(signupRequestDto);

        URI location = URI.create("/api/v1/accounts/" + accountResponseDto.id());
        return ResponseEntity.created(location).body(ApiResponse.success(accountResponseDto));
    }
}