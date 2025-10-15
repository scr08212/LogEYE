package com.nkm.logeye.domain.account;

import com.nkm.logeye.domain.account.dto.AccountResponseDto;
import com.nkm.logeye.domain.account.dto.SignupRequestDto;
import com.nkm.logeye.global.exception.EmailDuplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
        SignupRequestDto requestDto = new SignupRequestDto("test@example.com", "12345678", "TestUser");
        String encodedPassword = "encodedPassword";

        when(passwordEncoder.encode(requestDto.password())).thenReturn(encodedPassword);
        when(accountRepository.findByEmail(requestDto.email())).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        //when
        AccountResponseDto responseDto = accountService.signup(requestDto);

        //then
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());

        Account account = captor.getValue();
        assertThat(account.getEmail()).isEqualTo(requestDto.email());
        assertThat(account.getPassword()).isEqualTo(encodedPassword);
        assertThat(account.getName()).isEqualTo(requestDto.name());
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);

        verify(passwordEncoder).encode(requestDto.password());
        verify(accountRepository).findByEmail(requestDto.email());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_fail(){
        // given
        SignupRequestDto requestDto = new SignupRequestDto("test@example.com", "12345678", "TestUser");
        when(accountRepository.findByEmail(requestDto.email())).thenReturn(Optional.of(Account.builder().build()));
        // when then
        assertThatThrownBy(() -> accountService.signup(requestDto))
                .isInstanceOf(EmailDuplicationException.class);
    }
}