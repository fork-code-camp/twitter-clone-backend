package com.example.authentication.service;

import com.example.authentication.dto.ActivationCodeResponse;
import com.example.authentication.dto.AuthenticationRequest;
import com.example.authentication.dto.AuthenticationResponse;
import com.example.authentication.dto.RegisterRequest;
import com.example.authentication.entity.Account;
import com.example.authentication.entity.ActivationCode;
import com.example.authentication.entity.Token;
import com.example.authentication.exception.AccountNotActivatedException;
import com.example.authentication.exception.ActivationCodeExpiredException;
import com.example.authentication.exception.ActivationCodeNotFoundException;
import com.example.authentication.model.TokenType;
import com.example.authentication.repository.AccountRepository;
import com.example.authentication.repository.ActivationCodeRepository;
import com.example.authentication.repository.TokenRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static com.example.authentication.model.Role.USER;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AccountRepository accountRepository;
    private final TokenRepository tokenRepository;
    private final ActivationCodeRepository activationCodeRepository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public ActivationCodeResponse register(RegisterRequest request) {
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
                .isEnabled(false)
                .role(USER)
                .build();

        var savedUser = accountRepository.save(newAccount);
        sendNewActivationCode(newAccount);

        return ActivationCodeResponse.builder()
                .message("Activation code's been sent to your email!")
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow();

        if (!account.isEnabled()) {
            throw new AccountNotActivatedException("Account not activated!");
        }

        var jwtToken = jwtService.generateToken(account);

        revokeAllAccountTokens(account);
        saveAccountToken(account, jwtToken);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public ActivationCodeResponse activate(String key) {
        ActivationCode activationCode = activationCodeRepository.findActivationCodeByKey(key)
                .orElseThrow(() -> new ActivationCodeNotFoundException("Activation code not found: " + key));

        checkActivationCodeExpiration(activationCode);

        activationCode.getAccount().setEnabled(true);
        activationCodeRepository.deleteById(activationCode.getId());

        return ActivationCodeResponse.builder()
                .message("Account successfully activated!")
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

    private void checkActivationCodeExpiration(ActivationCode activationCode) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTime = activationCode.getExpirationTime();
        if (expirationTime.isBefore(now)) {
            activationCodeRepository.deleteById(activationCode.getId());
            sendNewActivationCode(activationCode.getAccount());
            long minutes = ChronoUnit.MINUTES.between(expirationTime, now);
            throw new ActivationCodeExpiredException(String.format("Activation code %s expired %d minutes ago. New activation code has been sent.", activationCode.getKey(), minutes));
        }
    }

    private void sendNewActivationCode(Account account) {
        ActivationCode activationCode = ActivationCode.builder()
                .account(account)
                .key(UUID.randomUUID().toString())
                .expirationTime(LocalDateTime.now().plusHours(2L))
                .build();

        emailService.sendActivationCode(activationCode);
        activationCodeRepository.save(activationCode);
    }
}
