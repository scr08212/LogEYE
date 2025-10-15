package com.nkm.logeye.domain.account;

import com.nkm.logeye.domain.account.dto.AccountResponseDto;
import com.nkm.logeye.domain.account.dto.SignupRequestDto;
import com.nkm.logeye.global.exception.EmailDuplicationException;
import com.nkm.logeye.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AccountResponseDto signup(SignupRequestDto signupRequestDto) {
        if(accountRepository.findByEmail(signupRequestDto.email()).isPresent()){
            throw new EmailDuplicationException();
        }

        String encodedPassword = passwordEncoder.encode(signupRequestDto.password());

        Account account = Account.builder()
                .email(signupRequestDto.email())
                .password(encodedPassword)
                .name(signupRequestDto.name())
                .status(AccountStatus.ACTIVE)
                .build();
        Account savedAccount = accountRepository.save(account);
        return AccountResponseDto.from(savedAccount);
    }
}
