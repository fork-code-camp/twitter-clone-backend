package com.example.authentication.service;

import com.example.authentication.entity.Account;
import com.example.authentication.entity.Token;
import com.example.authentication.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.authentication.model.TokenType.BEARER;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    public void createToken(Account account, String jwt) {
        tokenRepository.save(
                Token.builder()
                        .account(account)
                        .jwt(jwt)
                        .tokenType(BEARER)
                        .expired(false)
                        .revoked(false)
                        .build()
        );
    }

    public void deleteTokenByAccount(Account account) {
        tokenRepository.findByAccount_Id(account.getId())
                .ifPresent(tokenRepository::delete);
    }
}
