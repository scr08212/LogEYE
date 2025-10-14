package com.nkm.logeye.domain.account;

import com.nkm.logeye.domain.account.dto.AccountResponseDto;
import com.nkm.logeye.domain.account.dto.SignupRequestDto;
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
    public ResponseEntity<AccountResponseDto> signup(@RequestBody @Valid SignupRequestDto signupRequestDto) {
        Account savedAccount = accountService.signup(signupRequestDto);
        AccountResponseDto accountResponseDto = AccountResponseDto.from(savedAccount);

        URI location = URI.create("/api/v1/accounts/" + savedAccount.getId());
        return ResponseEntity.created(location).body(accountResponseDto);
    }
}