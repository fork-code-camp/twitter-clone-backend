package com.example.authentication.service;

import com.example.authentication.entity.Account;
import com.example.authentication.entity.Token;
import com.example.authentication.model.TokenType;
import com.example.authentication.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountTokenService {

    private final TokenRepository tokenRepository;

    public void saveAccountToken(Account account, String jwtToken) {
        var token = Token.builder()
                .account(account)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    public void deleteAccountToken(Account account) {
        tokenRepository.findByAccount_Id(account.getId())
                .ifPresent(tokenRepository::delete);
    }
}
