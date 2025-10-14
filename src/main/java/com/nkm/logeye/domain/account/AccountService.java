package com.nkm.logeye.domain.account;

import com.nkm.logeye.domain.account.dto.SignupRequestDto;
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
    public Account signup(SignupRequestDto signupRequestDto) {

        if(accountRepository.findByEmail(signupRequestDto.email()).isPresent()){
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequestDto.password());

        Account account = Account.builder()
                .email(signupRequestDto.email())
                .password(encodedPassword)
                .name(signupRequestDto.name())
                .status(AccountStatus.ACTIVE)
                .build();

        return accountRepository.save(account);
    }
}
