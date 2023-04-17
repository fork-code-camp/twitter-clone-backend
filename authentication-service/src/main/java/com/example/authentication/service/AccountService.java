package com.example.authentication.service;

import com.example.authentication.controller.dto.LoginRequest;
import com.example.authentication.controller.dto.RegisterRequest;
import com.example.authentication.model.Account;
import com.example.authentication.repository.AccountRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.example.authentication.model.Role.USER;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest registerRequest) {
        accountRepository.findByEmail(registerRequest.getEmail())
                .ifPresent(account -> {
                    throw new EntityExistsException("Account already exists: " + account.getEmail());
                });

        Account newAccount = Account.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .isAccountNonLocked(true)
                .isAccountNonExpired(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .role(USER)
                .build();

        accountRepository.saveAndFlush(newAccount);
    }

    public String login(LoginRequest loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ));
        return "you log in successfully";
    }
}
