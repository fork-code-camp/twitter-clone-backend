package com.example.authentication.service;

import com.example.authentication.entity.Account;
import com.example.authentication.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.example.authentication.model.Role.USER;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;


    public void enableAccount(Account account) {
        account.setEnabled(true);
        accountRepository.saveAndFlush(account);
    }

    public Optional<Account> findAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public Account createNewAccount(String email, String password) {
        return accountRepository.saveAndFlush(
                Account.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .isAccountNonLocked(true)
                .isAccountNonExpired(true)
                .isCredentialsNonExpired(true)
                .isEnabled(false)
                .role(USER)
                .build()
        );
    }
}
