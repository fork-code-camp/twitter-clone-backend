package com.example.authentication.service;

import com.example.authentication.dto.ActivationCodeResponse;
import com.example.authentication.dto.AuthenticationRequest;
import com.example.authentication.dto.AuthenticationResponse;
import com.example.authentication.dto.RegisterRequest;
import com.example.authentication.entity.Account;
import com.example.authentication.entity.ActivationCode;
import com.example.authentication.exception.AccountNotActivatedException;
import com.example.authentication.exception.ActivationCodeNotFoundException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final JwtService jwtService;
    private final ActivationCodeService activationCodeService;
    private final AccountService accountService;
    private final AccountTokenService accountTokenService;
    private final MessageSourceService messageService;

    public ActivationCodeResponse register(RegisterRequest request) {
        accountService.findAccountByEmail(request.getEmail())
                .ifPresent(account -> {
                    log.error("account with {} already exists", account.getEmail());
                    throw new EntityExistsException(messageService.generateMessage("error.account.already_exists", account.getEmail()));
                });

        Account newAccount = accountService.createNewAccount(request.getEmail(), request.getPassword());
        log.info("account {} has been created", newAccount.getId());

        activationCodeService.sendNewActivationCode(newAccount);
        log.info("activation code has been sent to {}", newAccount.getEmail());

        return ActivationCodeResponse.builder()
                .message(messageService.generateMessage("activation.send.success"))
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var account = accountService.findAccountByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("account by email {} not found", request.getEmail());
                    throw new EntityNotFoundException(messageService.generateMessage(
                            "error.entity.not_found", request.getEmail())
                    );
                });

        if (!account.isEnabled()) {
            log.error("account {} is not activated", account.getId());
            throw new AccountNotActivatedException(
                    messageService.generateMessage("error.account.not_activated", account.getEmail()
                    ));
        }

        var jwtToken = jwtService.generateToken(account);

        accountTokenService.deleteAccountToken(account);
        accountTokenService.saveAccountToken(account, jwtToken);
        log.info("jwt was generated {}", jwtToken);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public ActivationCodeResponse activate(String key) {
        ActivationCode activationCode = activationCodeService.findActivationCodeByKey(key)
                .orElseThrow(() -> {
                    log.error("activation code not found by key {}", key);
                    throw new ActivationCodeNotFoundException(
                            messageService.generateMessage("error.activation_code.not_found", key)
                    );
                });

        activationCodeService.checkActivationCodeExpiration(activationCode);
        accountService.enableAccount(activationCode.getAccount());
        activationCodeService.deleteActivationCodeById(activationCode.getId());

        log.info("account with email {} has been successfully activated", activationCode.getAccount().getEmail());
        return ActivationCodeResponse.builder()
                .message(messageService.generateMessage("account.activation.success"))
                .build();
    }
}
