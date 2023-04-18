package com.example.authentication.service;

import com.example.authentication.controller.dto.AuthenticationRequest;
import com.example.authentication.controller.dto.AuthenticationResponse;
import com.example.authentication.controller.dto.RegisterRequest;
import com.example.authentication.model.Account;
import com.example.authentication.model.Token;
import com.example.authentication.model.TokenType;
import com.example.authentication.repository.AccountRepository;
import com.example.authentication.repository.TokenRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.example.authentication.model.Role.USER;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AccountRepository accountRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        accountRepository.findByEmail(request.getEmail())
                .ifPresent(account -> {
                    throw new EntityExistsException("Account already exists: " + account.getEmail());
                });

        Account newAccount = Account.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountNonLocked(true)
                .isAccountNonExpired(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .role(USER)
                .build();

        var savedUser = accountRepository.save(newAccount);
        var jwtToken = jwtService.generateToken(newAccount);
        saveAccountToken(savedUser, jwtToken);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        ); // in case the email/password isn't correct -> exception will be thrown

        var account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(account);

        revokeAllAccountTokens(account);
        saveAccountToken(account, jwtToken);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    private void saveAccountToken(Account account, String jwtToken) {
        var token = Token.builder()
                .account(account)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllAccountTokens(Account account) {
        var validAccountTokens = tokenRepository.findAllValidTokenByAccount(account.getId());
        if (validAccountTokens.isEmpty())
            return;
        validAccountTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validAccountTokens);
    }
}
