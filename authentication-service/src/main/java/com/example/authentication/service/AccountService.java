package com.example.authentication.service;

import com.example.authentication.entity.Account;
import com.example.authentication.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.example.authentication.model.Role.USER;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSourceService messageService;

    public Account findAccountByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageService.generateMessage("error.entity.not_found", email)
                ));
    }

    public Account createNewAccount(String email, String password, boolean isEnabled) {
        return accountRepository.saveAndFlush(
                Account.builder()
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .isAccountNonLocked(true)
                        .isAccountNonExpired(true)
                        .isCredentialsNonExpired(true)
                        .isEnabled(isEnabled)
                        .role(USER)
                        .build()
        );
    }

    public void enableAccount(Account account) {
        account.setEnabled(true);
        accountRepository.saveAndFlush(account);
    }

    public boolean doesAccountExists(String email) {
        return accountRepository.findByEmail(email)
                .isPresent();
    }

    public void enableAccount(Account account) {
        account.setEnabled(true);
        accountRepository.saveAndFlush(account);
    }

    public boolean doesAccountExists(String email) {
        return accountRepository.findByEmail(email)
                .isPresent();
    }
}
