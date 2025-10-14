package com.nkm.logeye.domain.account;

import com.nkm.logeye.domain.account.dto.SignupRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // given (테스트 준비)
        SignupRequestDto requestDto = new SignupRequestDto("test@example.com", "password123", "TestUser");
        String encodedPassword = "encodedPassword";

        when(passwordEncoder.encode(requestDto.password())).thenReturn(encodedPassword);
        when(accountRepository.findByEmail(requestDto.email())).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        //when
        Account savedAccount = accountService.signup(requestDto);

        //then
        assertThat(savedAccount.getEmail()).isEqualTo(requestDto.email());
        assertThat(savedAccount.getPassword()).isEqualTo(encodedPassword);
        assertThat(savedAccount.getName()).isEqualTo(requestDto.name());
        assertThat(savedAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE);

        verify(passwordEncoder).encode(requestDto.password());
        verify(accountRepository).findByEmail(requestDto.email());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_fail(){
        // given
        SignupRequestDto requestDto = new SignupRequestDto("test@example.com", "password123", "TestUser");
        when(accountRepository.findByEmail(requestDto.email())).thenReturn(Optional.of(Account.builder().build()));
        // when then
        assertThatThrownBy(() -> accountService.signup(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(("이미 사용중인 이메일입니다."));
    }
}